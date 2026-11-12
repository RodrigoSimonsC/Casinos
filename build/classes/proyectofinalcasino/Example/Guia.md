# BlackCasino: organización y lógica del juego

Este documento describe la organización interna de BlackCasino y el funcionamiento
de su lógica de Blackjack. Se excluyen intencionalmente las ventanas, los
componentes Swing, las animaciones y cualquier otra implementación gráfica.

## 1. Organización del proyecto

La aplicación es un proyecto Java administrado por NetBeans. El código fuente
principal se encuentra en `src/blackcasino`:

```text
BlackCasino/
├── src/
│   └── blackcasino/
│       ├── LogicaDeJuego/
│       ├── elementosCasino/
│       ├── historial/
│       ├── modelos/
│       ├── negocio/
│       ├── interfaz/       # Capa gráfica; fuera del alcance de este documento
│       ├── animaciones/    # Capa gráfica; fuera del alcance de este documento
│       └── BlackCasino.java
```

### Paquetes que participan en la lógica

| Paquete | Responsabilidad |
|---|---|
| `blackcasino.modelos` | Entidades del juego: carta, jugador y crupier. |
| `blackcasino.elementosCasino` | Baraja y manos; administra las cartas que se utilizan durante una ronda. |
| `blackcasino.LogicaDeJuego` | Reglas de Blackjack, acciones sobre las manos, mesa, turnos y estados de la partida. |
| `blackcasino.negocio` | Apuestas, saldo de jugadores, caja del casino y liquidación de pagos. |
| `blackcasino.historial` | Resultado de una partida y persistencia opcional del historial en texto. |

`build/` y `dist/` no contienen la fuente de la lógica: son productos generados.
Los cambios deben realizarse en `src/`.

## 2. Modelo de dominio

### 2.1 `Carta`

Archivo: `src/blackcasino/modelos/Carta.java`

Una carta contiene:

- `valor`: `As`, `2` a `10`, `J`, `Q` o `K`.
- `palo`: `Corazones`, `Diamantes`, `Picas` o `Treboles`.
- `numeroMazo`: identifica el mazo de origen cuando se utilizan varios mazos.

La carta es inmutable. Sus métodos `obtenerValor()`, `obtenerPalo()` y
`obtenerNumeroMazo()` exponen sus datos, y `toString()` produce una
representación como `As de Corazones[0]`.

### 2.2 `Mano`

Archivo: `src/blackcasino/elementosCasino/Mano.java`

Una mano es una colección de cartas y un indicador `plantada`:

- `agregarCarta(Baraja, posicion)`: toma una carta de una posición concreta de
  la baraja.
- `agregarCarta(Carta)`: agrega directamente una carta, utilizado principalmente
  durante un split.
- `obtenerCartas()`: devuelve una copia inmodificable.
- `plantarse()`: impide que se pidan más cartas.
- `retirarUltimaCarta()`: extrae la última carta, necesario para dividir.
- `limpiar()`: elimina las cartas y restablece el estado de la mano.

La mano no elimina cartas de la baraja. El consumo se controla mediante la
posición `posicionSiguienteCarta` de `AccionesDeJuego`.

### 2.3 `Jugador`

Archivo: `src/blackcasino/modelos/Jugador.java`

El jugador conserva su nombre, saldo y una lista de manos. El saldo se almacena
con `BigDecimal` para evitar errores de precisión monetaria.

- `retirarSaldo(importe)` valida que el importe sea positivo y que exista saldo.
- `ingresarSaldo(importe)` aumenta el saldo después de validar el importe.
- `asignarMano(mano)` reemplaza todas las manos actuales.
- `agregarMano(mano)` permite añadir una mano, por ejemplo después de un split.
- `obtenerManos()` devuelve todas las manos.
- `obtenerMano()` devuelve la primera mano, que es la mano principal.

Una ronda normal utiliza una mano por jugador; un split puede añadir manos.

### 2.4 `Crupier`

Archivo: `src/blackcasino/modelos/Crupier.java`

El crupier posee una única `Mano`. No tiene saldo ni apuestas propias: su
comportamiento se ejecuta mediante `AccionesDeJuego.jugarDealer`.

## 3. Baraja y generación de cartas

Archivo: `src/blackcasino/elementosCasino/Baraja.java`

`Baraja` genera y mantiene las cartas disponibles:

1. Recibe la cantidad de mazos.
2. Si el valor es menor que uno, utiliza un mazo.
3. Genera 52 cartas por mazo mediante la combinación de cuatro palos y
   trece valores.
4. Guarda el índice del mazo de origen en cada carta.
5. Baraja la lista con Fisher-Yates usando `SecureRandom`.

`obtenerCartas()` entrega una vista inmodificable. La baraja no tiene una
operación de extracción: la carta que se reparte es la que ocupa la posición
actual y `AccionesDeJuego` incrementa esa posición.

`reiniciar()` elimina las cartas actuales, genera de nuevo la cantidad original
de mazos y vuelve a barajar. Esto prepara una nueva ronda sin reutilizar el
orden anterior.

## 4. Reglas de Blackjack

Archivo: `src/blackcasino/LogicaDeJuego/ReglasBlackjack.java`

Esta clase es utilitaria y concentra las reglas que no dependen de la mesa ni
de la interfaz.

### 4.1 Cálculo de puntuación

`calcularValor(...)` asigna:

- As: 11 inicialmente.
- `J`, `Q` y `K`: 10.
- Cartas numéricas: su valor entero.

Después de sumar, cada As puede convertirse de 11 a 1 restando 10 mientras el
total sea superior a 21. Así, `As + 9 + 5` se evalúa como 15 y no como 25.

La clase ofrece versiones que reciben una lista de cartas o una `Mano`.

### 4.2 Estados derivados de una mano

- `tieneBlackjack`: exactamente dos cartas con valor 21.
- `superaVeintiuno`: valor mayor que 21.
- `estaReventada`: alias para detectar una mano superior a 21.
- `estaPlantada`: consulta si el jugador dejó de pedir cartas.
- `puedeDividir`: exactamente dos cartas con el mismo valor. El palo no importa.

### 4.3 Resolución contra el crupier

`resolver(manoJugador, manoDealer)` aplica este orden:

1. Blackjack del jugador y no del crupier: `Blackjack: ganaste`.
2. Blackjack del crupier y no del jugador: `Perdiste: blackjack del dealer`.
3. Jugador superior a 21: `Perdiste: tu mano supera 21`.
4. Crupier superior a 21: `Ganaste: el dealer supera 21`.
5. Puntuación del jugador mayor: `Ganaste`.
6. Puntuación del jugador menor: `Perdiste`.
7. Puntuaciones iguales: `Empate`.

## 5. Flujo de acciones de juego

Archivo: `src/blackcasino/LogicaDeJuego/AccionesDeJuego.java`

`AccionesDeJuego` es el motor operativo de las cartas. Conserva:

- La `Baraja` activa.
- La posición de la próxima carta.
- El máximo de splits permitidos.
- El número de splits realizados.
- Un `RepartoListener` opcional para notificar repartos sin acoplar el motor
  con una capa de presentación.

### 5.1 Reparto inicial

`repartirInicial(jugadores)` valida que la lista no sea nula ni esté vacía y
comprueba que haya dos cartas por jugador. Reparte en dos vueltas:

```text
vuelta 1: una carta a cada jugador
vuelta 2: una carta a cada jugador
```

La variante que recibe la mano del dealer hace primero el reparto de jugadores
y después entrega dos cartas al crupier.

Si un jugador todavía no tiene mano, el motor crea su mano principal.

### 5.2 Pedir carta

`pedirCarta(...)`:

1. Valida jugador y mano cuando se reciben ambos.
2. Rechaza manos plantadas.
3. Comprueba que aún queden cartas.
4. Obtiene la carta en `posicionSiguienteCarta`.
5. Incrementa la posición.
6. Devuelve la carta y, cuando corresponde, notifica al listener.

La misma operación se usa para jugadores, manos divididas y crupier.

### 5.3 Plantarse

`plantarse(mano)` cambia el indicador de la mano a `true`. Desde ese momento
`pedirCarta` y `dividir` rechazan nuevas acciones sobre ella.

### 5.4 Split

`dividir(jugador, mano)` solamente permite dividir cuando:

- No se alcanzó `maximoSplits`.
- La mano no está plantada.
- Tiene exactamente dos cartas.
- Las dos cartas tienen el mismo valor.

El motor retira la última carta, crea una nueva mano, coloca allí la carta
retirada, agrega la nueva mano al jugador y entrega una carta adicional tanto a
la mano original como a la nueva.

El contador de splits se reinicia al comenzar una ronda y se incrementa después
de cada división.

### 5.5 Turno automático del crupier

`jugarDealer(manoDealer)` pide cartas mientras el valor sea menor que 17 y la
mano no esté reventada. Cuando alcanza 17 o más, el crupier se planta si no
supera 21.

La lógica actual no solicita cartas adicionales al crupier cuando ya está en
17 o más, aunque tenga un As que pueda cambiar de valor según la puntuación.

### 5.6 Reinicio

`reiniciarRonda(jugadores)`:

1. Restablece la posición de la próxima carta.
2. Restablece el contador de splits.
3. Reinicia y baraja la baraja.
4. Limpia todas las manos de los jugadores.

La mano del crupier se limpia desde `Partida.prepararNuevaRonda()`.

## 6. Mesa y control de turnos

### 6.1 `Mesa`

Archivo: `src/blackcasino/LogicaDeJuego/Mesa.java`

`Mesa` funciona como fachada del motor. Agrupa la baraja, la lista de jugadores
y `AccionesDeJuego`, y expone operaciones de alto nivel:

- Repartir cartas.
- Pedir carta.
- Plantarse.
- Dividir una mano.
- Ejecutar el turno del crupier.
- Preparar una nueva ronda.
- Consultar los jugadores.

Sus constructores permiten crear una mesa con una cantidad de mazos, una baraja
existente, jugadores y un máximo de splits. La lista de jugadores se copia para
evitar que la mesa cambie por modificaciones externas.

### 6.2 `ControlTurnos`

Archivo: `src/blackcasino/LogicaDeJuego/ControlTurnos.java`

Mantiene dos índices:

- `indiceJugador`: jugador actual.
- `indiceMano`: mano actual de ese jugador.

`obtenerManoActiva` devuelve la mano seleccionada. `avanzar` pasa a la
siguiente mano y, cuando no quedan manos del jugador actual, vuelve a la mano
inicial del siguiente jugador.

Es un componente de apoyo para coordinar turnos. El flujo principal de
`Partida` valida que se esté en la fase de jugadores, pero actualmente no
invoca directamente a `ControlTurnos`.

## 7. Estados y ciclo de una partida

### 7.1 Estados

Archivo: `src/blackcasino/LogicaDeJuego/EstadoPartida.java`

```text
ESPERANDO_APUESTAS
REPARTIENDO
TURNO_JUGADORES
TURNO_CRUPIER
CALCULANDO_RESULTADOS
FINALIZADA
```

### 7.2 `Partida`

Archivo: `src/blackcasino/LogicaDeJuego/Partida.java`

`Partida` coordina jugadores, crupier, mesa y apuestas. Inicia en
`ESPERANDO_APUESTAS`.

El ciclo esperado es:

```text
ESPERANDO_APUESTAS
        |
        | todos los jugadores apuestan
        v
REPARTIENDO
        |
        | reparto inicial
        v
TURNO_JUGADORES
        |
        | pedir, plantarse o dividir
        v
TURNO_CRUPIER
        |
        | crupier juega automáticamente
        v
CALCULANDO_RESULTADOS
        |
        | liquidación/finalización
        v
FINALIZADA
        |
        | prepararNuevaRonda
        v
ESPERANDO_APUESTAS
```

Operaciones principales:

- `apostarJugador(indice)`: registra la apuesta durante
  `ESPERANDO_APUESTAS`.
- `retirarApuestaJugador(indice)`: devuelve la apuesta antes del reparto.
- `iniciarReparto()`: exige que todos hayan apostado, reparte jugadores y
  crupier y pasa a `TURNO_JUGADORES`.
- `pedirCartaJugador(indice)`: permite pedir durante `TURNO_JUGADORES`.
- `plantarJugador(indice)`: marca la mano como plantada.
- `dividirJugador(indice)`: solicita un split a la mesa.
- `doblarApuestaJugador(indice)`: duplica la apuesta de la mano principal.
- `iniciarTurnoDealer()`: cambia a `TURNO_CRUPIER` y ejecuta el crupier.
- `finalizar()`: solo permite cerrar desde `TURNO_CRUPIER` o
  `CALCULANDO_RESULTADOS`.
- `prepararNuevaRonda()`: exige `FINALIZADA`, reinicia mesa y manos y vuelve a
  `ESPERANDO_APUESTAS`.

Cada método valida el estado requerido y lanza `IllegalStateException` si se
intenta ejecutar una acción fuera de orden. También valida los índices de
jugador y la existencia de una mesa cuando la operación la necesita.

## 8. Apuestas, saldos y pagos

### 8.1 `Apuesta`

Archivo: `src/blackcasino/negocio/Apuesta.java`

Representa un importe positivo. `doblar()` multiplica el importe por dos.

### 8.2 `Caja`

Archivo: `src/blackcasino/negocio/Caja.java`

La caja mantiene un saldo `BigDecimal`:

- `ingresar(importe)` suma dinero.
- `retirar(importe)` resta dinero solo si existe saldo suficiente.

Ambas operaciones rechazan importes nulos, cero o negativos.

### 8.3 `GestorApuestas`

Archivo: `src/blackcasino/negocio/GestorApuestas.java`

Gestiona dos relaciones:

- `Jugador -> Apuesta`: apuesta general del jugador.
- `Mano -> Apuesta`: asociación necesaria para manos divididas o dobladas.

#### Registro

`registrarApuesta`:

1. Comprueba que el jugador no tenga otra apuesta.
2. Retira el importe del saldo del jugador.
3. Ingresa el importe en la caja.
4. Guarda la apuesta y la asocia a la primera mano disponible.

`todosApostaron(cantidadJugadores)` comprueba que la cantidad de apuestas
registradas coincida con la cantidad de jugadores.

#### Retiro

`retirarApuesta` elimina la apuesta, devuelve el importe al jugador y lo retira
de la caja.

#### Double

`doblar` cobra nuevamente un importe igual al de la apuesta actual, lo ingresa
en la caja y duplica la apuesta asociada a la mano.

#### Split y liquidación

`duplicarParaSplit` existe para cobrar una segunda apuesta igual a la original
y asociarla a la nueva mano. Sin embargo, `Partida.dividirJugador` actualmente
solo solicita la división a `Mesa`; no invoca automáticamente
`duplicarParaSplit`. Si se integra el split con apuestas, esa operación debe
realizarse explícitamente.

`liquidar` resuelve la mano contra el crupier y calcula el pago:

| Resultado | Pago total entregado |
|---|---:|
| Empate | `1.00 ×` la apuesta |
| Blackjack del jugador | `2.50 ×` la apuesta |
| Victoria normal | `2.00 ×` la apuesta |
| Derrota o bust | `0.00` |

Cuando el pago es mayor que cero, se retira de la caja y se ingresa al saldo
del jugador. El método devuelve un `ResultadoPartida`.

## 9. Historial de resultados

### 9.1 `ResultadoPartida`

Archivo: `src/blackcasino/historial/ResultadoPartida.java`

Es un objeto de datos que conserva:

- Jugador.
- Importe pagado.
- Texto del resultado.

### 9.2 `HistorialPartidas`

Archivo: `src/blackcasino/historial/HistorialPartidas.java`

El historial puede utilizarse:

- Solo en memoria, con el constructor sin argumentos.
- Con persistencia en un archivo, pasando un `Path`.

`registrar` agrega el resultado a la colección y lo guarda si hay archivo
configurado. `obtenerResultados` devuelve una copia inmodificable.

El formato de cada línea es:

```text
nombre|resultado|pago
```

Al cargar, se leen líneas UTF-8 y se reconstruyen los resultados. El carácter
`|` se reemplaza por `/` dentro del texto del resultado al guardar para no
romper la separación de campos. Los errores de lectura, escritura o conversión
del importe se informan mediante `IllegalStateException`.

## 10. Secuencia lógica completa

Una ejecución típica desde código sigue esta secuencia:

1. Crear jugadores con un saldo inicial.
2. Crear un `Crupier`.
3. Crear una `Mesa` con la cantidad de mazos, jugadores y máximo de splits.
4. Crear una `Partida` asociada a esa mesa.
5. Registrar una apuesta por jugador.
6. Iniciar el reparto cuando todos hayan apostado.
7. Ejecutar las acciones de cada jugador: pedir, plantarse, dividir o doblar.
8. Iniciar el turno automático del crupier.
9. Resolver cada mano contra la mano del crupier.
10. Liquidar las apuestas y registrar los `ResultadoPartida`.
11. Finalizar la partida.
12. Preparar la siguiente ronda, que reinicia baraja y manos.

La lógica está desacoplada de la presentación mediante `RepartoListener`. Este
listener solo comunica que comenzó el reparto o que se repartió una carta; no
calcula puntuaciones, decide ganadores ni modifica las reglas.

## 11. Validaciones y errores importantes

El motor protege el estado del juego mediante validaciones explícitas:

- No se aceptan jugadores, cartas, manos, barajas o importes `null` cuando no
  corresponda.
- No se permiten saldos, apuestas o importes negativos.
- No se puede apostar dos veces al mismo jugador.
- No se puede pedir carta a una mano plantada.
- No se puede dividir una mano que no tenga dos cartas del mismo valor.
- No se puede superar el máximo de splits.
- No se puede repartir si no hay cartas suficientes.
- No se puede iniciar el reparto si falta una apuesta.
- No se puede avanzar la partida desde un estado incorrecto.
- No se puede pagar más dinero del disponible en la caja.

La mayoría de las violaciones de reglas producen `IllegalStateException` y las
entradas inválidas producen `IllegalArgumentException`, `NullPointerException`
o `IndexOutOfBoundsException` según la validación involucrada. Esto evita que
una acción inválida se convierta silenciosamente en un estado incoherente.
