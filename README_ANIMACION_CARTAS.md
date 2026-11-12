# Animación de cartas de Blackjack

Este documento explica cómo se construye y se anima la mesa de Blackjack en
Java Swing. La implementación principal está en
[`AnimacionReparto.java`](src/proyectofinalcasino/interfaz/AnimacionReparto.java).

La animación no usa imágenes individuales para las cartas ni una librería
externa. Cada carta se representa como un objeto con coordenadas y se dibuja
directamente con `Graphics2D`. Un `javax.swing.Timer` cambia esas coordenadas
periódicamente y Swing vuelve a pintar el panel.

---

## 1. Componentes que participan

### `VentanaPartida`

Archivo:

```text
src/proyectofinalcasino/interfaz/VentanaPartida.java
```

La ventana:

1. Carga la imagen de fondo `Minimalist Poker Table Layout.png`.
2. Crea un objeto `AnimacionReparto`.
3. Agrega ese objeto al centro de la ventana.
4. Conecta los eventos de la animación con `PartidaControlador`.

La imagen de la mesa es solamente el fondo. Las cartas se dibujan encima en
`AnimacionReparto`.

### `AnimacionReparto`

Es un `JPanel` transparente:

```java
public final class AnimacionReparto extends JPanel
```

Se encarga de:

- Crear la cola de cartas.
- Calcular el destino de cada carta.
- Mover cartas durante la animación.
- Dibujar el frente o reverso.
- Controlar turnos, dealer, pedir, doblar y separar.
- Volver a pintar la mesa después de cada cambio.

### `CartaReparto`

Es una clase interna de `AnimacionReparto`. No es un componente Swing
individual. Es un objeto de datos que conserva:

```java
valor
jugador
dealer
ronda
mano
posicionEnMano
x
y
inicioX
inicioY
destinoX
destinoY
```

Por eso una carta puede desplazarse sin crear un `JLabel` o un componente
nuevo para cada carta.

---

## 2. Cómo se crea el diseño visual

El diseño se produce combinando tres capas:

1. **Fondo:** la imagen de la mesa se dibuja en `FondoMesa`, dentro de
   `VentanaPartida`.
2. **Cartas y textos:** `AnimacionReparto.paintComponent(...)` dibuja las
   cartas, los valores, el turno y los resultados.
3. **Fichas:** `FichaCasinoPanel` dibuja las fichas como paneles Swing
   transparentes colocados con `setBounds(...)`.

El panel de cartas usa:

```java
setOpaque(false);
setLayout(null);
```

`setOpaque(false)` permite ver el fondo de la mesa. El diseño absoluto permite
colocar cartas y fichas en coordenadas calculadas a partir del tamaño actual
del panel.

### Tamaños principales

```java
private static final int CARTA_ANCHO = 74;
private static final int CARTA_ALTO = 106;
```

Todas las cartas se dibujan con esas dimensiones. Si se cambian, también se
modifican automáticamente las separaciones y varias posiciones porque estas
usan `CARTA_ANCHO` y `CARTA_ALTO`.

---

## 3. Flujo completo de una animación

### 3.1 La baraja proporciona las cartas

El constructor recibe una `BarajaBlackjack` y guarda sus cartas:

```java
cartasBaraja = baraja.obtenerCartas();
crearCola(cartasBaraja);
```

`AnimacionReparto` no decide el orden de la baraja. Ese orden ya viene de
`BarajaBlackjack`.

### 3.2 Se prepara la cola de reparto

`crearCola(...)` recorre dos rondas:

```java
for (int ronda = 0; ronda < 2; ronda++) {
    for (int jugador = 0; jugador < jugadores.size(); jugador++) {
        pendientes.add(...);
    }
    pendientes.add(...); // carta del dealer
}
```

El orden es:

1. Primera carta del jugador 1.
2. Primera carta del jugador 2.
3. Primera carta de cada jugador restante.
4. Primera carta del dealer.
5. Segunda carta de cada jugador.
6. Segunda carta del dealer.

Cada elemento se guarda en `pendientes`, una `Deque<CartaReparto>`.

### 3.3 Se inicia el reloj

El reloj se crea así:

```java
reloj = new Timer(35, evento -> actualizar());
```

Esto significa que Swing llama a `actualizar()` aproximadamente cada 35
milisegundos. La animación comienza cuando se ejecuta:

```java
reloj.start();
```

Ese inicio ocurre en `iniciarPartida()` y también cuando el jugador pide una
carta, dobla o separa.

El `Timer` de Swing se ejecuta en el Event Dispatch Thread, el hilo de eventos
de la interfaz. Por eso el movimiento y el repintado permanecen coordinados
con los controles Swing.

---

## 4. Cómo se consigue el deslizamiento

El método central es `actualizar()`.

### 4.1 Se toma una carta pendiente

Cuando no hay una carta en movimiento:

```java
cartaActual = pendientes.pollFirst();
progreso = 0;
cartaActual.configurarDestino();
```

La carta comienza en:

```java
inicioX = 100;
inicioY = 100;
x = inicioX;
y = inicioY;
```

Su destino se calcula según si pertenece a un jugador o al dealer.

### 4.2 Se avanza el progreso

Cada llamada al temporizador incrementa:

```java
progreso++;
```

La duración lógica es:

```java
private static final int DURACION_MOVIMIENTO = 15;
```

Con un intervalo de 35 ms, el movimiento tarda aproximadamente:

```text
15 × 35 ms = 525 ms
```

El tiempo real puede variar ligeramente según la carga del sistema.

### 4.3 Se calcula la posición intermedia

Mientras no termina el movimiento:

```java
cartaActual.x = interpolar(
        cartaActual.inicioX,
        cartaActual.destinoX,
        progreso);

cartaActual.y = interpolar(
        cartaActual.inicioY,
        cartaActual.destinoY,
        progreso);
```

La fórmula es:

```java
private int interpolar(int inicio, int destino, int paso) {
    double avance = paso / (double) DURACION_MOVIMIENTO;
    return (int) (inicio + (destino - inicio) * avance);
}
```

En términos matemáticos:

```text
posición = inicio + (destino - inicio) × avance
```

Ejemplo conceptual:

```text
inicio  = 100
destino = 700
paso    = 7
avance  = 7 / 15 = 0.466
posición aproximada = 380
```

Como el valor cambia poco a poco, la carta parece deslizarse desde el mazo
hacia su posición final.

### 4.4 Se fuerza el repintado

Al final de cada actualización se llama:

```java
repaint();
```

Swing programa un nuevo repintado del panel. En ese repintado,
`paintComponent(...)` usa los valores actuales de `x` y `y`, por lo que la
carta aparece en la nueva posición.

### 4.5 Se finaliza la carta

Cuando `progreso` llega a `DURACION_MOVIMIENTO`:

```java
cartaActual.x = cartaActual.destinoX;
cartaActual.y = cartaActual.destinoY;
repartidas.add(cartaActual);
cartaActual = null;
pausa = PAUSA_ENTRE_CARTAS;
```

La carta deja de ser la carta animada y pasa a la lista permanente
`repartidas`. Después se deja una pausa:

```java
private static final int PAUSA_ENTRE_CARTAS = 8;
```

Con el reloj actual, la pausa es de aproximadamente 280 ms.

---

## 5. Cómo se calcula la posición final

### Jugadores

`calcularPosicionHorizontal(...)` distribuye las manos horizontalmente:

- Usa el ancho del panel.
- Respeta `MARGEN_MESA`.
- Calcula una separación según el número de jugadores.
- Centra todo el conjunto en la ventana.

```java
private static final int MARGEN_MESA = 40;
```

La posición vertical de los jugadores se basa en:

```java
getHeight() - 205
```

Las cartas adicionales de una mano se desplazan usando:

```java
DESPLAZAMIENTO_CARTA = 0.20;
```

La separación horizontal entre cartas es aproximadamente el 20 % del ancho
de una carta.

### Dealer

El dealer se centra horizontalmente. Sus cartas se colocan cerca de:

```java
y = 145 + ronda * desplazamientoY;
```

La segunda carta del dealer se pinta inicialmente boca abajo.

### Split

Cuando se separan cartas, `calcularPosicionMano(...)` mueve las dos manos:

```java
SEPARACION_MANOS = 150;
```

La mano izquierda usa la mitad negativa de esa separación y la mano derecha
la mitad positiva.

---

## 6. Cómo se dibujan las cartas

`paintComponent(...)` es el ciclo de renderizado:

```java
for (CartaReparto carta : repartidas) {
    pintarCarta(g, carta);
}

if (cartaActual != null) {
    pintarCarta(g, cartaActual);
}
```

Primero dibuja las cartas que ya llegaron y después la carta que está
deslizándose.

### Reverso

`pintarReverso(...)` dibuja:

- Una sombra negra desplazada tres píxeles.
- Un rectángulo rojo con esquinas redondeadas.
- Un borde blanco.
- Un marco interior.

### Frente

`pintarFrente(...)`:

1. Separa el valor y el palo del texto de la carta.
2. Convierte el palo a un símbolo:
   - `♥` para corazones.
   - `♦` para diamantes.
   - `♣` para tréboles.
   - `♠` para espadas.
3. Colorea corazones y diamantes en rojo.
4. Dibuja sombra, fondo blanco y borde.
5. Escribe valor y palo en las esquinas.
6. Dibuja un símbolo grande en el centro.

El redondeo se obtiene con:

```java
new RoundRectangle2D.Double(
        x, y, CARTA_ANCHO, CARTA_ALTO, 12, 12);
```

El efecto visual se mejora con antialiasing:

```java
g.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
```

### Carta oculta

`estaOculta()` devuelve verdadero para la segunda carta del dealer mientras
`dealerRevelado` sea falso.

Además, la carta actualmente en movimiento se muestra inicialmente por el
reverso:

```java
cartaActual == carta && progreso < DURACION_MOVIMIENTO / 2
```

Esto crea el efecto de que la carta sale boca abajo y después revela su frente
al avanzar el movimiento.

---

## 7. Acciones que lanzan nuevas animaciones

### Reparto inicial

`iniciarPartida()`:

1. Valida las apuestas.
2. Marca la partida como iniciada.
3. Reinicia los estados de jugadores.
4. Arranca `reloj`.

### Pedir carta

`pedirCarta()`:

1. Valida que el jugador pueda pedir.
2. Obtiene la siguiente carta de `cartasBaraja`.
3. La agrega a `pendientes`.
4. Llama a `iniciarAnimacion()`.

### Doblar

`doblarApuesta()` hace lo mismo que pedir carta, pero marca la mano como
doblada y finalizada después de recibir esa carta.

### Separar

`separarCartas()`:

1. Cambia la segunda carta de la mano 0 a la mano 1.
2. Activa `animandoSeparacion`.
3. Reinicia `progreso`.
4. Arranca el reloj.

`actualizarSeparacion()` mueve las dos cartas existentes a sus nuevas
posiciones. Al terminar, agrega una carta nueva a cada mano y continúa el
reparto normal.

### Dealer

Cuando terminan los jugadores, `iniciarTurnoDealer()` activa:

```java
dealerRevelado = true;
turnoDealer = true;
```

Después, `pedirCartaDealer()` agrega cartas a la cola hasta que el dealer
alcanza 17 o más, o hasta que se determina el resultado.

---

## 8. Máquina de estados visual

Los booleanos de `AnimacionReparto` representan el estado de la animación y de
la ronda:

| Estado | Significado |
|---|---|
| `partidaIniciada` | La ronda fue iniciada |
| `finalizada` | Terminó el movimiento de cartas actual o el reparto pendiente |
| `turnoDealer` | El turno actual pertenece al dealer |
| `dealerRevelado` | La carta oculta del dealer puede mostrarse |
| `turnoFinalizado` | Ya no se permiten decisiones del jugador |
| `separado` | La mano inicial fue dividida |
| `animandoSeparacion` | Las cartas del split todavía se están moviendo |
| `resultado` | La ronda ya tiene un resultado calculado |

`actualizar()` consulta estos estados para decidir si debe:

1. Animar una separación.
2. Esperar una pausa.
3. Tomar otra carta de la cola.
4. Mover la carta actual.
5. Detener el reloj y pasar al siguiente turno.

---

## 9. Reinicio de la ronda

Después de mostrar el resultado, se crea un `Timer` de una sola ejecución de
3500 ms. Ese temporizador llama a `reiniciarRonda()`.

El reinicio:

- Detiene el reloj principal.
- Vacía `pendientes` y `repartidas`.
- Reinicia posiciones y contadores.
- Reinicia estados de dealer, split y turnos.
- Elimina las fichas visuales.
- Crea una nueva baraja.
- Vuelve a crear la cola de reparto.
- Marca que hay una nueva ronda pendiente.

La ventana exterior detecta esa marca y prepara de nuevo al controlador.

---

## 10. Cómo modificar la animación

### Hacer el movimiento más rápido

Reducir:

```java
DURACION_MOVIMIENTO
```

o reducir el intervalo del `Timer`:

```java
new Timer(35, ...)
```

Cambiar ambos valores puede hacer la animación demasiado rápida para
percibirse o aumentar la carga de repintado.

### Hacer el movimiento más lento

Aumentar `DURACION_MOVIMIENTO` conserva el mismo intervalo de repintado y
produce más pasos intermedios. Aumentar el intervalo del `Timer` reduce la
frecuencia de actualización, pero puede hacer que el movimiento se vea
entrecortado.

### Cambiar el punto de salida

Modificar en `CartaReparto`:

```java
inicioX = 100;
inicioY = 100;
```

Todas las cartas salen actualmente del mismo punto visual.

### Cambiar el tamaño de las cartas

Modificar:

```java
CARTA_ANCHO
CARTA_ALTO
```

También conviene revisar el tamaño de fuente, los márgenes internos y el
desplazamiento entre cartas.

### Cambiar la distancia entre cartas

Modificar:

```java
DESPLAZAMIENTO_CARTA
```

Un valor mayor separa más las cartas de una misma mano.

### Cambiar la separación del split

Modificar:

```java
SEPARACION_MANOS
```

---

## 11. Resumen del algoritmo

```text
Crear AnimacionReparto
    ↓
Guardar cartas de la baraja
    ↓
Crear cola de reparto
    ↓
Iniciar javax.swing.Timer
    ↓
Tomar carta pendiente
    ↓
Calcular destino
    ↓
Repetir 15 pasos:
    calcular posición intermedia
    actualizar x e y
    repaint()
    ↓
Guardar carta en repartidas
    ↓
Esperar 8 ciclos
    ↓
Repetir con la siguiente carta
    ↓
Dibujar cartas, valores y turnos
    ↓
Continuar turno del jugador o dealer
```

La idea fundamental es separar **estado**, **movimiento** y **dibujo**:

- `CartaReparto` conserva el estado geométrico.
- `Timer` y `actualizar()` cambian la posición.
- `paintComponent()` muestra el estado actual.

Esa separación es la que permite conseguir la animación sin utilizar
componentes gráficos individuales para cada carta.
