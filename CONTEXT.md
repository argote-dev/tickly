# Tickly

Temporizador personal para practicar la técnica Pomodoro alternando enfoque y descanso.

## Language

**Bloque de enfoque**:
Periodo dedicado a trabajar con concentración, con una duración elegida por la persona.
_Avoid_: Sesión (ambiguo entre un bloque y varios bloques).

**Descanso corto**:
Periodo breve de descanso entre bloques de enfoque.

**Descanso largo**:
Periodo de descanso tras completar una cantidad determinada de bloques de enfoque.

**Duración del intervalo**:
Tiempo configurado para un bloque de enfoque, un descanso corto o un descanso largo.
_Avoid_: Intervalo (sin aclarar si se refiere al periodo o a su duración).

**Enfoque completado**:
Bloque de enfoque cuyo tiempo ha terminado; un bloque abandonado o saltado no es un enfoque completado.

**Avance hacia el descanso largo**:
Cantidad de enfoques completados que se acumulan para alcanzar el próximo descanso largo. Saltar un descanso corto no elimina por sí solo los enfoques completados.

**Tanda**:
Ciclo de enfoques completados y descansos que termina al salir de un descanso largo. Al salir de ese descanso se completa la tanda y se reinicia el avance hacia el siguiente descanso largo.

**Reiniciar intervalo**:
Acción que devuelve el intervalo actual a su duración inicial sin borrar el avance hacia el descanso largo.
_Avoid_: Reiniciar (sin especificar el alcance).

**Empezar de cero**:
Acción que borra el avance y vuelve al enfoque, a la espera de un inicio manual.

**Intervalo finalizado**:
Intervalo cuyo tiempo ha terminado y que espera confirmación para iniciar el siguiente; no implica que el siguiente ya esté corriendo.
