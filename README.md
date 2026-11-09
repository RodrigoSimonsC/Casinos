# Proyecto Final Casino - Blackjack

Aplicación de casino desarrollada en Java Swing para administrar una mesa de
Blackjack con jugadores, apuestas, fichas, turnos, cartas, resultados,
recompensas, recargas de saldo y persistencia en MySQL mediante JDBC.

Este documento describe el sistema desde la entrada de la aplicación hasta el
último registro almacenado en la base de datos. La explicación está organizada
por paquetes y clases para que sea posible seguir el flujo prácticamente
método por método.

---

## 1. Objetivo del proyecto

El programa permite:

1. Configurar una mesa de Blackjack.
2. Agregar jugadores y asignarles saldo.
3. Consultar el saldo actual almacenado en MySQL.
4. Iniciar una sesión de juego.
5. Seleccionar el jugador que realiza una acción.
6. Apostar y retirar fichas antes de comenzar la ronda.
7. Repartir cartas con animación Swing.
8. Pedir carta, plantarse, doblar y separar.
9. Ejecutar el turno del dealer.
10. Determinar victorias, derrotas, empates y blackjack.
11. Aplicar pagos y actualizar saldos.
12. Recargar el saldo de un jugador.
13. Registrar la partida y sus eventos en MySQL.
14. Repetir el ciclo de rondas.

El sistema utiliza exclusivamente Swing para la interfaz. La aplicación no
depende de una API HTTP ni de un cliente web.

---

## 2. Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 25 | Lenguaje principal |
| Swing | Ventanas, paneles, botones, tablas y diálogos |
| JDBC | Comunicación con MySQL |
| MySQL | Usuarios, saldos, partidas y eventos |
| NetBeans | Estructura original del proyecto |
| `mysql-connector-j-9.4.0.jar` | Driver JDBC |

El punto de entrada es:

```text
src/proyectofinalcasino/ProyectoFinalCasino.java
```

---

## 3. Estructura de paquetes

```text
src/
└── proyectofinalcasino/
    ├── ProyectoFinalCasino.java
    ├── conexiones/
    │   ├── JdbcConexion.java
    │   ├── MesaRepository.java
    │   ├── MesaRepositoryJdbc.java
    │   ├── MesaRepositoryMemoria.java
    │   ├── PartidaRepository.java
    │   └── PartidaRepositoryJdbc.java
    ├── funciones/
    │   ├── BarajaBlackjack.java
    │   ├── EstadoPartida.java
    │   ├── GestorApuestas.java
    │   ├── GestorCartas.java
    │   ├── GestorMesa.java
    │   ├── PartidaControlador.java
    │   ├── PartidaEventoListener.java
    │   ├── PartidaPersistencia.java
    │   └── ResultadoBlackjack.java
    ├── interfaz/
    │   ├── AnimacionReparto.java
    │   ├── FichaCasinoPanel.java
    │   ├── PanelConfiguracion.java
    │   ├── PanelControlesPartida.java
    │   ├── PanelJugadores.java
    │   ├── VentanaPartida.java
    │   └── VentanaPrincipal.java
    └── modelo/
        ├── ConfiguracionMesa.java
        └── Jugador.java
```

### Responsabilidad de cada paquete

#### `modelo`

Contiene datos simples del dominio. Estas clases no deben abrir conexiones,
dibujar componentes ni decidir el flujo de una ronda.

#### `conexiones`

Contiene JDBC, repositorios y SQL. Su responsabilidad es convertir datos entre
MySQL y objetos Java.

#### `funciones`

Contiene reglas, coordinación de partida, validaciones y servicios de dominio.
No debe construir ventanas Swing.

#### `interfaz`

Contiene ventanas y paneles Swing. La interfaz muestra información y delega
las decisiones de negocio al controlador.

---

## 4. Inicio de la aplicación

### `ProyectoFinalCasino`

La clase de entrada crea la ventana principal en el hilo de eventos de Swing.

Flujo conceptual:

```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(VentanaPrincipal::new);
}
```

El uso de `SwingUtilities.invokeLater` garantiza que la interfaz sea creada
en el Event Dispatch Thread, que es el hilo recomendado para Swing.

La clase de inicio no debe:

- Crear una partida directamente.
- Ejecutar SQL.
- Resolver cartas.
- Aplicar pagos.
- Crear servidores HTTP.

Su única tarea es arrancar la aplicación visual.

---

## 5. Modelo de datos Java

## 5.1 `Jugador`

Archivo:

```text
src/proyectofinalcasino/modelo/Jugador.java
```

Representa a una persona sentada en la mesa.

### Atributos

```java
private final String nombre;
private final double saldo;
```

- `nombre`: identificación visual y lógica del jugador.
- `saldo`: saldo actual mostrado en la mesa.

Los atributos son `final`, por lo que un objeto `Jugador` es inmutable.
Cuando cambia el saldo se crea otro objeto `Jugador`.

### Métodos

| Método | Responsabilidad |
|---|---|
| `Jugador(String, double)` | Crea el jugador |
| `getNombre()` | Devuelve el nombre |
| `getSaldo()` | Devuelve el saldo |
| `toString()` | Devuelve el nombre para controles Swing |

El saldo no se modifica directamente en el modelo. El cambio pasa por
`GestorMesa`, que valida y persiste la operación.

---

## 5.2 `ConfiguracionMesa`

Archivo:

```text
src/proyectofinalcasino/modelo/ConfiguracionMesa.java
```

Representa la configuración que se utilizará para una sesión.

Incluye normalmente:

- Cantidad de barajas.
- Máximo de jugadores.
- Múltiplo de apuesta.
- Variante de reglas.
- Efectos de sonido.
- Recarga automática.

La interfaz modifica esta clase mediante setters y el repositorio la guarda en
`configuracion_mesa`.

La configuración es usada por:

- `GestorMesa` para validaciones.
- `BarajaBlackjack` para determinar la cantidad de cartas.
- `GestorApuestas` para determinar el valor de cada ficha.
- `PartidaRepositoryJdbc` para relacionar la sesión con una configuración.

---

## 6. Conexión y persistencia general

## 6.1 `JdbcConexion`

Archivo:

```text
src/proyectofinalcasino/conexiones/JdbcConexion.java
```

Es la fábrica central de conexiones JDBC.

Configuración predeterminada:

```text
URL:      jdbc:mysql://localhost:3306/casino
Usuario:  root
Password: vacío
```

También acepta propiedades de JVM:

```text
-Ddb.url=jdbc:mysql://localhost:3306/casino
-Ddb.user=root
-Ddb.password=tu_password
```

La URL utiliza:

- `useSSL=false`
- `serverTimezone=UTC`
- `allowPublicKeyRetrieval=true`

### Responsabilidad

`JdbcConexion` solamente abre conexiones. No conoce jugadores, partidas ni
componentes visuales.

### Manejo de errores

Si no logra conectarse, devuelve `null` y escribe un mensaje de diagnóstico.
Los repositorios interpretan esa situación y permiten que la aplicación
muestre el error o continúe en memoria cuando corresponde.

---

## 6.2 `MesaRepository`

Archivo:

```text
src/proyectofinalcasino/conexiones/MesaRepository.java
```

Es la interfaz del repositorio de la mesa.

Métodos principales:

```java
List<Jugador> consultarJugadores();
List<Jugador> consultarHistorialJugadores();
void guardarJugadores(List<Jugador> jugadores);
boolean actualizarSaldo(String nombre, double saldo);
ConfiguracionMesa consultarConfiguracion();
void guardarConfiguracion(ConfiguracionMesa configuracion);
```

Esta interfaz permite sustituir MySQL por memoria durante pruebas.

---

## 6.3 `MesaRepositoryJdbc`

Archivo:

```text
src/proyectofinalcasino/conexiones/MesaRepositoryJdbc.java
```

Implementa `MesaRepository` utilizando las tablas:

- `Usuarios`
- `estadoUsuario`
- `configuracion_mesa`

### Jugadores activos

El repositorio mantiene una lista en memoria llamada `jugadoresActivos`.
La lista representa quién está sentado actualmente en la mesa.

La base de datos mantiene el historial general, pero retirar a un jugador de la
mesa no elimina su registro de `Usuarios`.

### Sincronización de saldos

Antes de devolver los jugadores activos, el repositorio consulta nuevamente:

```sql
SELECT u.Nombre, eu.saldo_global
FROM Usuarios u
INNER JOIN estadoUsuario eu ON eu.id_Usuario = u.id_Usuarios
WHERE u.Nombre = ?
```

Esto evita que la configuración muestre un saldo viejo después de:

- Apostar.
- Perder.
- Ganar.
- Empatar.
- Recargar.
- Volver de una partida.

### Guardar jugadores

El método `guardarJugadores`:

1. Abre una conexión.
2. Desactiva temporalmente el autocommit.
3. Recorre la lista recibida.
4. Busca si ya existe el jugador.
5. Inserta en `Usuarios` si no existe.
6. Obtiene la clave generada.
7. Inserta el saldo en `estadoUsuario`.
8. Confirma la transacción.
9. Restaura el autocommit.
10. Actualiza la lista activa.

### Actualizar saldo

El método `actualizarSaldo` ejecuta:

```sql
UPDATE estadoUsuario eu
INNER JOIN Usuarios u ON u.id_Usuarios = eu.id_Usuario
SET eu.saldo_global = ?
WHERE u.Nombre = ?
```

Después de actualizar MySQL, reemplaza el objeto `Jugador` correspondiente en
la lista activa.

### Compatibilidad de columnas

El repositorio detecta si existen columnas opcionales de configuración. Esto
permite trabajar con una base antigua que todavía no tenga todas las columnas.

---

## 6.4 `MesaRepositoryMemoria`

Implementa la misma interfaz sin MySQL.

Es útil para:

- Pruebas.
- Desarrollo visual.
- Verificar reglas sin depender del servidor de base de datos.

El código de interfaz utiliza la abstracción `MesaRepository`, por lo que no
necesita saber si está usando memoria o JDBC.

---

## 7. Baraja y reglas de cartas

## 7.1 `BarajaBlackjack`

Archivo:

```text
src/proyectofinalcasino/funciones/BarajaBlackjack.java
```

Genera y mezcla las cartas.

### Valores

```text
As, 2, 3, 4, 5, 6, 7, 8, 9, 10, J, Q, K
```

### Palos

```text
Corazones, Diamantes, Treboles, Picas
```

Una carta se almacena como texto:

```text
10 de Diamantes (baraja 6)
```

La referencia a la baraja es útil para demostrar qué mazo produjo la carta.

### Mezcla

Utiliza `SecureRandom` y un algoritmo tipo Fisher-Yates:

1. Comienza en la última posición.
2. Elige una posición aleatoria.
3. Intercambia ambas cartas.
4. Continúa hacia el inicio.

### Métodos

| Método | Responsabilidad |
|---|---|
| `BarajaBlackjack(int)` | Crea una o más barajas |
| `BarajaBlackjack(ConfiguracionMesa)` | Usa la configuración |
| `obtenerCartas()` | Devuelve una copia inmutable |
| `imprimirPosiciones()` | Imprime diagnóstico de la baraja |

---

## 7.2 `ResultadoBlackjack`

Contiene las reglas matemáticas de Blackjack:

- Valor de cartas.
- Tratamiento del as como 1 u 11.
- Blackjack natural.
- Bust.
- Victoria.
- Derrota.
- Empate.

La clase no dibuja, no abre ventanas y no actualiza dinero.

---

## 7.3 `GestorCartas`

Es una fachada para las operaciones de cartas.

Métodos importantes:

| Método | Uso |
|---|---|
| `calcularValor` | Calcula la puntuación |
| `tieneBlackjack` | Detecta dos cartas con 21 |
| `superaVeintiuno` | Detecta bust |
| `valorVisible` | Produce texto para la interfaz |
| `puedeSeparar` | Verifica que las cartas tengan el mismo valor |
| `resolver` | Compara jugador y dealer |

La interfaz llama a estos métodos indirectamente a través de la partida. Así,
el cálculo de reglas no depende de Swing.

---

## 8. Sistema de apuestas

## 8.1 `GestorApuestas`

Archivo:

```text
src/proyectofinalcasino/funciones/GestorApuestas.java
```

Administra únicamente las apuestas temporales de la ronda.

### Atributos

```java
private final int multiplo;
private final int[] apuestas;
```

- `multiplo`: valor de una ficha.
- `apuestas`: apuesta acumulada por índice de jugador.

El índice del arreglo corresponde al orden de la lista de jugadores.

### Constructor

```java
public GestorApuestas(int cantidadJugadores, int multiplo)
```

Si el múltiplo es menor que uno, se reemplaza por `1`.
La cantidad negativa de jugadores se convierte en cero.

### Agregar ficha

```java
agregarFicha(int jugador, boolean partidaIniciada)
```

Rechaza la operación si:

- La partida ya comenzó.
- El índice no existe.

Si es válida, suma el múltiplo a la apuesta del jugador.

El saldo se valida fuera de esta clase, en el controlador, porque esta clase
solo conoce fichas y cantidades, no conoce MySQL ni jugadores.

### Retirar ficha

```java
retirarFicha(int jugador, boolean partidaIniciada)
```

Resta una ficha si:

- La partida no comenzó.
- El índice es válido.
- La apuesta tiene al menos el valor del múltiplo.

Nunca permite apuestas negativas.

### Todos apostaron

```java
todosApostaron()
```

Devuelve `true` solamente si existe al menos un jugador y todas las apuestas
son mayores que cero.

### Reiniciar

```java
reiniciar()
```

Coloca todas las apuestas en cero al preparar una nueva ronda.

---

## 8.2 Flujo real del saldo y la apuesta

Antes de iniciar la ronda:

1. `GestorApuestas` acumula fichas.
2. `PartidaControlador` consulta los saldos reales.
3. Verifica que cada apuesta sea positiva.
4. Verifica que cada jugador pueda pagarla.
5. Descuenta la apuesta del saldo.
6. Guarda el saldo actualizado.
7. Registra la ronda.
8. Inicia el reparto.

Si falla el registro de la ronda:

1. Se restauran los saldos descontados.
2. Se sincroniza la restauración.
3. La ronda no comienza.

Al finalizar:

- Victoria: retorno total de 2.0 veces la apuesta.
- Blackjack: retorno total de 2.5 veces la apuesta.
- Empate: retorno total de 1.0 vez la apuesta.
- Derrota: retorno 0.

Como la apuesta ya fue descontada al comenzar, los multiplicadores representan
el retorno total y no una ganancia adicional.

---

## 9. Control del ciclo de partida

## 9.1 `EstadoPartida`

Enumera los estados:

```text
ESPERANDO_APUESTAS
REPARTIENDO
TURNO_JUGADOR
TURNO_DEALER
RESULTADO
```

Los estados evitan que una acción ocurra en una fase incorrecta.

---

## 9.2 `PartidaControlador`

Archivo:

```text
src/proyectofinalcasino/funciones/PartidaControlador.java
```

Es el coordinador de negocio de la sesión.

No debe:

- Crear botones.
- Dibujar cartas.
- Crear ventanas.
- Ejecutar consultas SQL directamente.

### Abrir partida

`abrir` delega en `PartidaPersistencia` la creación de la sesión.

### Validar apuesta

`puedeApostar` comprueba:

1. Estado `ESPERANDO_APUESTAS`.
2. Índice válido.
3. Saldo suficiente para la siguiente ficha.

### Iniciar ronda

`iniciarRonda`:

1. Comprueba el estado.
2. Comprueba que el arreglo tenga el mismo tamaño que la lista.
3. Comprueba que todas las apuestas sean positivas.
4. Comprueba saldos.
5. Descuenta cada apuesta.
6. Registra los saldos.
7. Crea la ronda persistida.
8. Restaura saldos si la persistencia falla.
9. Cambia a `REPARTIENDO`.

### Pagos

`pagar`:

1. Verifica el índice.
2. Obtiene el nombre.
3. Aplica el pago positivo.
4. Registra el pago.
5. Registra el saldo actualizado.

### Nueva ronda

`prepararNuevaRonda` cambia de `RESULTADO` a
`ESPERANDO_APUESTAS`.

### Eventos

El controlador implementa `PartidaEventoListener`. Así puede recibir:

- Cartas repartidas.
- Inicio de turnos.
- Decisiones.
- Resultado final.

Esto conecta el motor de partida con la persistencia sin que el panel ejecute
SQL.

---

## 10. Eventos de partida

## 10.1 `PartidaEventoListener`

Define los eventos del dominio:

```java
void cartaRepartida(int jugador, int mano, String carta, boolean dealer);
void turnoIniciado(int jugador, int mano, boolean dealer);
void decisionTomada(
        int jugador,
        int mano,
        String decision,
        double valorAdicional);
void partidaFinalizada(String[] resultados, int[] puntuaciones);
```

El emisor no necesita conocer quién recibe los eventos.

Esto representa el patrón Observer:

```text
Motor de partida -> PartidaEventoListener -> Persistencia
```

---

## 11. Persistencia de una partida

## 11.1 `PartidaRepository`

Define operaciones de persistencia:

- Crear partida.
- Crear ronda.
- Crear mano.
- Guardar cartas.
- Crear turnos.
- Guardar decisiones.
- Guardar resultados.
- Guardar pagos.
- Actualizar saldo de mesa.
- Cerrar partida.

El controlador depende de esta abstracción de forma indirecta a través de
`PartidaPersistencia`.

---

## 11.2 `PartidaPersistencia`

Coordina el registro de eventos y mantiene los identificadores relacionados:

```java
private int partidaId;
private int rondaId;
private int numeroRonda;
private int ordenTurno;
private Map<Integer, Integer> manos;
```

El mapa `manos` relaciona:

```text
índice del jugador -> id_mano de MySQL
```

### Apertura de partida

Llama a `crearPartida` y almacena el ID devuelto.

### Apertura de ronda

1. Aumenta el número de ronda.
2. Inserta la ronda.
3. Limpia el mapa de manos.
4. Reinicia el orden de turnos.
5. Crea una mano principal por jugador.
6. Guarda cada `id_mano`.

### Carta repartida

Recibe el evento y lo transforma en un registro `Repartido`.

Para jugador:

- Usa `id_mano`.
- Usa `id_estadoUsuario`.

Para dealer:

- `id_mano` queda nulo.
- `id_Jugador` queda nulo.
- `es_crupier` queda verdadero.

### Turno

Registra quién actuó, si fue dealer y cuál fue el orden.

### Decisión

Registra:

- Turno.
- Mano.
- Tipo de decisión.
- Valor adicional, usado principalmente en doblar.

### Resultado

Registra la puntuación y el texto final de cada jugador.

---

## 11.3 `PartidaRepositoryJdbc`

Es la implementación SQL.

### Creación de partida

Busca la última configuración:

```sql
SELECT id_configuracion
FROM configuracion_mesa
ORDER BY id_configuracion DESC
LIMIT 1
```

Después inserta:

```sql
INSERT INTO partida
(id_configuracion, estado_partida)
VALUES (?, 'En Curso')
```

Finalmente registra cada jugador en `partidaJugada`.

### Identificadores

El esquema utiliza dos identificadores relacionados:

1. `Usuarios.id_Usuarios`
   - Identidad general del usuario.
2. `estadoUsuario.id_estadoUsuario`
   - Estado monetario del usuario.

`partidaJugada` utiliza el ID general de `Usuarios`.
Las tablas operativas de cartas, manos, turnos, resultados y pagos utilizan el
ID de `estadoUsuario`, de acuerdo con las claves foráneas del esquema.

### Cartas

La columna debe tener suficiente capacidad:

```sql
CartaActual VARCHAR(255) NOT NULL
```

Esto permite valores como:

```text
10 de Diamantes (baraja 6)
```

El constructor del repositorio intenta asegurar automáticamente esa longitud
con `ALTER TABLE`. También existe una migración SQL en:

```text
src/proyectofinalcasino/Example/examples
```

Migración manual:

```sql
ALTER TABLE Repartido
MODIFY CartaActual VARCHAR(255) NOT NULL;
```

---

## 12. Esquema MySQL

El esquema completo está documentado en:

```text
src/proyectofinalcasino/Example/examples
```

## 12.1 `Usuarios`

Catálogo general:

```text
id_Usuarios
Nombre
```

## 12.2 `estadoUsuario`

Saldo global:

```text
id_estadoUsuario
id_Usuario
saldo_global
```

## 12.3 `configuracion_mesa`

Configuración persistida:

```text
id_configuracion
cantidad_mazos
maximo_jugadores
multiples_apuesta
variante_reglas
efectos_sonido
recarga_automatica
```

## 12.4 `partida`

Una sesión de juego:

```text
id_partida
id_configuracion
fecha_inicio
estado_partida
```

## 12.5 `partidaJugada`

Relaciona una partida con sus jugadores:

```text
id_partidaJugada
id_Partida
id_Usuario
monto_ingreso
saldo_actual_mesa
```

## 12.6 `rondas`

Historial de cada ciclo de apuestas:

```text
id_rondas
id_PartidaJugada
ronda
```

## 12.7 `manos_jugador`

Manos principales o separadas:

```text
id_mano
id_ronda
idUsuario
numero_mano
apuesta_mano
estado_mano
```

## 12.8 `turnos`

Secuencia de acciones:

```text
id_turno
id_ronda
idUsuario
es_dealer
orden_turno
estado_turno
```

## 12.9 `decisiones_turno`

Acciones concretas:

```text
id_decision
id_turno
id_mano
tipo_decision
valor_adicional
fecha_hora
```

## 12.10 `resultados_ronda`

Resultado de cada jugador:

```text
id_resultado
id_ronda
idUsuario
puntuacion_final
resultado
```

## 12.11 `Repartido`

Todas las cartas:

```text
id_Repartido
id_Rondas
id_mano
id_Jugador
CartaActual
es_crupier
```

El dealer se distingue con `es_crupier = TRUE`.

## 12.12 `Apuesta`

Monto apostado por jugador y ronda:

```text
id_Apuesta
idUsuario
id_ronda
apostado
```

## 12.13 `Pagos`

Retorno económico:

```text
id_Pagos
id_ronda
idUsuario
monto_pagado
```

---

## 13. Interfaz Swing

## 13.1 `VentanaPrincipal`

Es la ventana administrativa.

Contiene:

- Encabezado.
- Pestaña de jugadores.
- Pestaña de configuración.
- Botón para comenzar la partida.

Al comenzar:

1. Solicita una baraja al `GestorMesa`.
2. Verifica que existan jugadores.
3. Oculta la ventana principal.
4. Crea `VentanaPartida`.

No crea partidas web ni servidores.

---

## 13.2 `PanelJugadores`

Administra la tabla de jugadores.

Funciones:

- Mostrar nombre.
- Mostrar saldo.
- Agregar jugador.
- Retirar jugador activo.
- Consultar historial.

La tabla no edita directamente el saldo. Las operaciones pasan por
`GestorMesa`.

---

## 13.3 `PanelConfiguracion`

Permite modificar:

- Barajas.
- Máximo de jugadores.
- Múltiplo de apuesta.
- Variante.
- Sonido.
- Recarga automática.

La persistencia de configuración se realiza a través de `MesaRepository`.

---

## 13.4 `PanelControlesPartida`

Contiene exclusivamente los controles de la partida:

- Pedir carta.
- Selector de jugador.
- Plantarse.
- Doblar.
- Separar.
- Apostar ficha.
- Retirar ficha.
- Comenzar partida.

El panel utiliza la interfaz interna `Acciones` para notificar eventos. No
conoce:

- MySQL.
- Saldos.
- Reglas de Blackjack.
- Multiplicadores.
- IDs de partida.

El selector tiene `setFocusable(false)` para no robar el foco de la acción de
teclado del dealer.

---

## 13.5 `FichaCasinoPanel`

Representa visualmente una ficha.

Responsabilidades:

- Dibujar la ficha.
- Mostrar el valor.
- Aplicar color según el monto.

No modifica saldos ni decide si una apuesta es válida.

---

## 13.6 `VentanaPartida`

Es la composición visual de la mesa activa.

Coordina:

- Fondo de la mesa.
- Barra superior.
- Saldo mostrado.
- Menú de recarga.
- Panel de controles.
- Panel de cartas.
- Diálogos.

La lógica principal se delega a:

- `PartidaControlador` para el negocio.
- `AnimacionReparto` para la visualización y acciones visuales.
- `GestorMesa` para jugadores y saldo.

### Recarga de saldo

El menú `Saldo -> Recargar saldo`:

1. Permite escoger un jugador.
2. Solicita un monto.
3. Valida que sea numérico.
4. Valida que sea mayor que cero.
5. Actualiza MySQL.
6. Actualiza el texto de saldos.

Cuando un jugador queda en cero, aparece un diálogo con campo de texto para
recargar inmediatamente o dejarlo para después.

---

## 14. Animación de cartas

## `AnimacionReparto`

Archivo:

```text
src/proyectofinalcasino/interfaz/AnimacionReparto.java
```

Es un `JPanel` que pinta cartas y ejecuta el movimiento visual usando un
`javax.swing.Timer`.

### Elementos visuales

- Cartas del jugador.
- Cartas del dealer.
- Fichas.
- Valores.
- Resultados individuales.
- Indicador de turno.
- Resaltado del jugador activo.

### Movimiento

Cada carta tiene:

```java
inicioX
inicioY
destinoX
destinoY
x
y
```

El método `interpolar` mueve gradualmente la carta desde su inicio hacia el
destino.

### Reparto

El reparto inicial alterna:

```text
Jugador 1
Jugador 2
...
Dealer
Jugador 1
Jugador 2
...
Dealer
```

La cola `pendientes` conserva el orden.

### Cartas adicionales

Las cartas del jugador se distribuyen en diagonal usando:

```text
posición X = posición de la mano + desplazamiento horizontal
posición Y = posición base + desplazamiento vertical
```

Esto evita que la tercera y siguientes cartas se superpongan en la misma
línea.

### Teclado

La ventana registra la tecla Espacio para pedir carta al dealer durante su
turno. El selector de jugador no recibe foco para no interferir.

### Límite de responsabilidad

La animación debe ser considerada la capa visual. En la versión actual todavía
conserva parte del estado necesario para sincronizar cartas, manos y eventos
visuales. La regla de saldo, el registro SQL y los pagos ya están delegados al
controlador y a la persistencia.

---

## 15. Flujo completo de una sesión

### Paso 1: aplicación

```text
ProyectoFinalCasino
    -> VentanaPrincipal
```

### Paso 2: jugadores

```text
PanelJugadores
    -> GestorMesa
        -> MesaRepository
            -> MesaRepositoryJdbc
                -> MySQL
```

### Paso 3: configuración

```text
PanelConfiguracion
    -> GestorMesa
        -> guardarConfiguracion
            -> configuracion_mesa
```

### Paso 4: abrir mesa

```text
VentanaPrincipal
    -> GestorMesa.comenzarPartida()
        -> BarajaBlackjack
    -> VentanaPartida
```

### Paso 5: sesión

```text
VentanaPartida
    -> PartidaControlador
        -> PartidaPersistencia
            -> PartidaRepositoryJdbc
                -> partida
                -> partidaJugada
```

### Paso 6: apuestas

```text
PanelControlesPartida
    -> AnimacionReparto.apostar()
        -> GestorApuestas
```

El botón se habilita solamente si el controlador confirma saldo suficiente.

### Paso 7: comenzar ronda

```text
PartidaControlador.iniciarRonda()
    -> validar apuestas
    -> descontar saldos
    -> registrar saldos
    -> crear ronda
    -> crear manos
    -> iniciar reparto
```

### Paso 8: cartas

```text
AnimacionReparto
    -> cartaRepartida(...)
        -> PartidaControlador
            -> PartidaPersistencia
                -> Repartido
```

### Paso 9: acciones

```text
pedir / plantarse / doblar / separar
    -> decisionTomada(...)
        -> decisiones_turno
```

### Paso 10: dealer

```text
todos los jugadores finalizan
    -> turno del dealer
    -> cartas del dealer
    -> resultado
```

### Paso 11: pagos

```text
resultado
    -> PartidaControlador.pagar()
        -> actualizar saldo
        -> registrar Pagos
        -> registrar saldo en partida
```

### Paso 12: siguiente ronda

```text
resultado
    -> limpiar cartas y fichas
    -> reiniciar apuestas
    -> EstadoPartida.ESPERANDO_APUESTAS
```

---

## 16. Patrones de diseño utilizados

## 16.1 Repository

Interfaces:

```text
MesaRepository
PartidaRepository
```

Implementaciones:

```text
MesaRepositoryJdbc
MesaRepositoryMemoria
PartidaRepositoryJdbc
```

Permite separar el dominio de la tecnología de almacenamiento.

## 16.2 Observer

Interfaz:

```text
PartidaEventoListener
```

El motor publica eventos y el controlador/persistencia los recibe.

Esto evita que el motor conozca directamente las consultas SQL.

## 16.3 Facade

Clases:

```text
GestorMesa
GestorCartas
PartidaControlador
```

Cada una ofrece una interfaz simplificada sobre operaciones más complejas.

## 16.4 MVC por capas

La aplicación se organiza conceptualmente así:

```text
Modelo:
    Jugador
    ConfiguracionMesa

Vista:
    VentanaPrincipal
    VentanaPartida
    Paneles
    AnimacionReparto

Control:
    GestorMesa
    PartidaControlador
    GestorCartas
    GestorApuestas

Persistencia:
    Repositories JDBC
```

---

## 17. Instalación de MySQL

1. Instalar MySQL o XAMPP.
2. Crear la base:

```sql
CREATE DATABASE casino;
```

3. Seleccionarla:

```sql
USE casino;
```

4. Ejecutar el contenido de:

```text
src/proyectofinalcasino/Example/examples
```

5. Verificar que `CartaActual` tenga tamaño suficiente:

```sql
ALTER TABLE Repartido
MODIFY CartaActual VARCHAR(255) NOT NULL;
```

6. Confirmar que el usuario configurado tenga permisos de:

- `SELECT`
- `INSERT`
- `UPDATE`
- `ALTER` si se desea permitir la migración automática de cartas

Si no se desea otorgar `ALTER`, ejecutar la migración manual y retirar ese
permiso del usuario de la aplicación.

---

## 18. Compilación manual

Desde la raíz del proyecto:

```powershell
$out = Join-Path $env:TEMP "casino-classes"
Remove-Item $out -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $out

javac --release 25 `
  -encoding UTF-8 `
  -cp "lib\mysql-connector-j-9.4.0.jar" `
  -d $out `
  (Get-ChildItem -Recurse -Filter *.java src |
      ForEach-Object { $_.FullName })
```

La compilación exitosa no significa que MySQL esté disponible; únicamente
confirma que las clases Java son válidas.

---

## 19. Ejecución

Con las clases compiladas:

```powershell
java `
  -cp "$out;lib\mysql-connector-j-9.4.0.jar" `
  proyectofinalcasino.ProyectoFinalCasino
```

Con propiedades personalizadas:

```powershell
java `
  -Ddb.url="jdbc:mysql://localhost:3306/casino" `
  -Ddb.user="root" `
  -Ddb.password="" `
  -cp "$out;lib\mysql-connector-j-9.4.0.jar" `
  proyectofinalcasino.ProyectoFinalCasino
```

---

## 20. Errores frecuentes

### `No se pudo conectar con la base de datos`

Revisar:

1. MySQL está iniciado.
2. La base `casino` existe.
3. El puerto es `3306`.
4. El usuario y contraseña son correctos.
5. El driver está en el classpath.

### `Data truncation: CartaActual`

Ejecutar:

```sql
ALTER TABLE Repartido
MODIFY CartaActual VARCHAR(255) NOT NULL;
```

La carta incluye el nombre del valor, el palo y el número de baraja.

### El saldo no coincide

La fuente actual es:

```text
estadoUsuario.saldo_global
```

Cerrar y volver a abrir la vista, o ejecutar:

```sql
SELECT u.Nombre, eu.saldo_global
FROM Usuarios u
INNER JOIN estadoUsuario eu
ON eu.id_Usuario = u.id_Usuarios;
```

### El botón de apostar está deshabilitado

Puede ocurrir si:

- El saldo es cero.
- El saldo no alcanza para otra ficha.
- La ronda ya comenzó.
- El jugador seleccionado ya terminó.
- El múltiplo configurado es mayor que el saldo.

### La tecla Espacio no pide carta

Revisar:

- Es el turno del dealer.
- El dealer aún no alcanzó 17.
- El selector no tenga foco.
- El `KeyStroke` esté registrado en el `JRootPane`.

### Las cartas no aparecen correctamente

Revisar:

- El tamaño del panel.
- El cálculo de `destinoX`.
- El cálculo de `destinoY`.
- La cantidad de cartas disponibles.
- La lista `pendientes`.

---

## 21. Pruebas manuales recomendadas

### Jugadores

1. Agregar un jugador con saldo válido.
2. Agregar un jugador con nombre vacío.
3. Agregar un jugador con saldo negativo.
4. Retirar un jugador.
5. Consultar historial.

### Apuestas

1. Apostar una ficha.
2. Apostar varias fichas.
3. Retirar una ficha.
4. Retirar hasta cero.
5. Intentar apostar con saldo cero.
6. Intentar apostar más del saldo disponible.
7. Intentar comenzar sin que todos apuesten.

### Ronda

1. Comenzar una ronda válida.
2. Pedir carta.
3. Plantarse.
4. Doblar.
5. Separar.
6. Completar varios jugadores.
7. Ejecutar dealer.
8. Verificar resultado individual.

### Saldo

1. Verificar descuento de apuesta.
2. Verificar pago de victoria.
3. Verificar pago de blackjack.
4. Verificar devolución de empate.
5. Verificar pérdida.
6. Recargar desde el menú.
7. Recargar desde el diálogo de saldo agotado.

### Persistencia

Después de una ronda, consultar:

```sql
SELECT * FROM partida ORDER BY id_partida DESC;
SELECT * FROM partidaJugada ORDER BY id_partidaJugada DESC;
SELECT * FROM rondas ORDER BY id_rondas DESC;
SELECT * FROM manos_jugador ORDER BY id_mano DESC;
SELECT * FROM turnos ORDER BY id_turno DESC;
SELECT * FROM decisiones_turno ORDER BY id_decision DESC;
SELECT * FROM Repartido ORDER BY id_Repartido DESC;
SELECT * FROM Apuesta ORDER BY id_Apuesta DESC;
SELECT * FROM resultados_ronda ORDER BY id_resultado DESC;
SELECT * FROM Pagos ORDER BY id_Pagos DESC;
```

---

## 22. Reglas de integridad del sistema

1. No se puede iniciar una ronda sin jugadores.
2. Todos los jugadores deben tener una apuesta positiva.
3. No se puede apostar después de iniciar el reparto.
4. El saldo no puede ser menor que cero.
5. Las fichas se agregan en múltiplos configurados.
6. Las fichas no pueden retirarse durante la partida.
7. Un jugador con saldo cero no puede colocar fichas.
8. El dealer actúa después de los jugadores.
9. Los pagos se aplican una sola vez por resultado.
10. Cada carta pertenece a una ronda.
11. Cada carta de jugador pertenece a una mano.
12. Las cartas del dealer se identifican con `es_crupier`.
13. Las decisiones se relacionan con un turno y una mano.
14. Las rondas se relacionan con una partida.
15. La configuración queda asociada a la partida.

---

## 23. Convenciones de código

- Clases en `PascalCase`.
- Métodos y atributos en `camelCase`.
- Constantes en `MAYUSCULAS_CON_GUION_BAJO`.
- Atributos privados.
- Interfaces para repositorios y eventos.
- JavaDoc breve para responsabilidades públicas.
- SQL parametrizado con `PreparedStatement`.
- Recursos JDBC cerrados con `try-with-resources`.
- Sin SQL dentro de los paneles.
- Sin componentes Swing dentro de los repositorios.
- Sin acceso directo de la vista a las tablas.

---

## 24. Qué clase debe modificarse para cada cambio

| Necesidad | Clase recomendada |
|---|---|
| Cambiar valor de ficha | `ConfiguracionMesa`, `GestorApuestas`, `PanelConfiguracion` |
| Cambiar validación de saldo | `PartidaControlador` |
| Cambiar cálculo de Blackjack | `ResultadoBlackjack` |
| Cambiar generación de cartas | `BarajaBlackjack` |
| Cambiar SQL de jugadores | `MesaRepositoryJdbc` |
| Cambiar SQL de partidas | `PartidaRepositoryJdbc` |
| Cambiar tablas o relaciones | `Example/examples` y repositorio correspondiente |
| Cambiar botones | `PanelControlesPartida` |
| Cambiar ventana principal | `VentanaPrincipal` |
| Cambiar diálogo de recarga | `VentanaPartida` |
| Cambiar movimiento de cartas | `AnimacionReparto` |
| Cambiar representación de ficha | `FichaCasinoPanel` |
| Cambiar estados | `EstadoPartida` y `PartidaControlador` |
| Agregar evento persistible | `PartidaEventoListener`, `PartidaControlador`, `PartidaPersistencia`, `PartidaRepository` |

---

## 25. Resumen final

El flujo general es:

```text
Usuario
  -> Swing
  -> Gestor/Controlador
  -> Reglas de Blackjack
  -> Persistencia JDBC
  -> MySQL
```

La interfaz no debe decidir reglas económicas. El repositorio no debe dibujar.
El modelo no debe abrir conexiones. El motor visual debe concentrarse en
representar y animar el estado que recibe del dominio.

La separación permite continuar agregando funcionalidades sin convertir una
sola clase en responsable de ventanas, reglas, apuestas, cartas, pagos y SQL
al mismo tiempo.
