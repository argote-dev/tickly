# Diseño de producto: Tickly

**Estado:** Implementado y validado en simuladores; ver [validación](validation.md). Los límites de dispositivos físicos y de plataformas siguen documentados allí.

Tickly será un temporizador Pomodoro personal, local y sin conexión. Su propósito es resolver la falta de usabilidad y personalización de los temporizadores existentes para su usuario principal, sin convertirse en un gestor de productividad.

La terminología del producto se define en el [glosario](../CONTEXT.md). La decisión de interfaces nativas por plataforma está registrada en el [ADR 0001](adr/0001-platform-native-ui.md).

## Alcance inicial

- Un temporizador Pomodoro excelente: enfoque, descanso corto y descanso largo.
- Configuración local de las duraciones y de la cantidad de bloques de enfoque antes de un descanso largo.
- Valores iniciales aceptados: 25 minutos de enfoque, 5 de descanso corto, 15 de descanso largo y un descanso largo cada cuatro enfoques completados.
- Límites de configuración aceptados: enfoque de 1 a 180 minutos; descansos corto y largo de 1 a 60 minutos; descanso largo después de 2 a 8 bloques.
- Operación completamente local y offline; no habrá cuentas ni sincronización entre dispositivos.
- Aplicación para teléfonos Android y iPhone; no se diseña una experiencia específica para tablet en esta versión.
- Internacionalización desde el inicio en español e inglés.

No son parte del alcance inicial: tareas, estadísticas, rachas, objetivos, perfiles de intervalos guardados, un editor de apariencia, widgets, Live Activities, una cuenta regresiva persistente en la pantalla bloqueada, importar sonidos, seleccionar colores arbitrarios ni una experiencia específica para tablet.

## Flujo del temporizador

- Los dos tipos de inicio son manuales: ni un descanso ni el siguiente enfoque se inician automáticamente.
- Un bloque de enfoque cuenta para el avance hacia el descanso largo únicamente cuando termina. Pausar, reiniciar, abandonar o saltar un enfoque no lo cuenta.
- Al saltar un enfoque se ofrece un descanso corto, sin iniciarlo automáticamente. Saltar un descanso corto devuelve al enfoque y no borra el avance acumulado.
- Al completar o saltar un descanso largo se cierra la tanda: se reinicia el avance hacia el próximo descanso largo y se vuelve al enfoque, a la espera de inicio manual.
- Se puede pausar, reiniciar o saltar cualquier intervalo. Pausar no requiere confirmación; reiniciar o abandonar un enfoque sí la requieren.
- **Reiniciar intervalo** restablece solamente el intervalo en curso y conserva el avance. **Empezar de cero** borra el avance, vuelve al enfoque y espera un inicio manual.
- Los ajustes modificados durante un intervalo no alteran ese intervalo; se aplican al siguiente que se inicie.
- Si se cambia la frecuencia del descanso largo con un intervalo en marcha, este no se interrumpe y un descanso ya pendiente no se sustituye. La nueva frecuencia se evalúa al finalizar el siguiente enfoque; si el nuevo umbral ya se alcanzó, corresponde descanso largo.

## Persistencia y avisos

- El estado se conserva incluso si la aplicación se cierra o se recupera en otro día.
- Al volver, un intervalo que estaba activo se reconstruye con el tiempo restante; uno ya terminado se muestra finalizado y esperando confirmación; uno pausado sigue pausado.
- La aplicación no avanza automáticamente por varios intervalos durante la ausencia.
- Al finalizar se intentará emitir un aviso único y discreto, con sonido y vibración opcionales, nunca una alarma insistente. Su entrega y sonido dependen de los permisos y restricciones del sistema; no se garantiza un aviso universal, por ejemplo tras una detención forzada.
- Los permisos de avisos se solicitarán con contexto al iniciar el primer temporizador. Si se deniegan, el temporizador sigue utilizable y la interfaz comunica que los avisos están desactivados.
- La aplicación no intentará eludir el silencio ni No molestar. En Android también explicará cuando falte el acceso necesario para avisos precisos.
- Existirá la opción de mantener la pantalla encendida durante el enfoque, desactivada inicialmente.

## Personalización

- Se configurarán las tres duraciones y la frecuencia del descanso largo dentro de los límites definidos.
- Habrá seis colores de acento prediseñados.
- Habrá tres sonidos suaves con previsualización, además de silencio.
- La vibración será opcional.
- El degradado animado de fondo podrá activarse o desactivarse.

## Dirección visual e interfaces

La experiencia debe ser serena, premium y minimalista: el tiempo ayuda a concentrarse, no compite por atención. Será exclusivamente en modo oscuro.

- **Pantalla principal:** cuenta regresiva grande, progreso circular sutil, nombre del intervalo, indicador de bloques completados y un botón principal de iniciar/pausar. Reiniciar y saltar tienen menor protagonismo; los ajustes deben estar a un toque.
- **Fondo:** degradado lento y orgánico, sin pulsar cada segundo, derivado del color de acento. Cuando se solicite reducción de movimiento será estático y siempre se podrá desactivar.
- **Android:** UI nativa en Compose con la versión estable más reciente de Material compatible con el proyecto.
- **iOS:** UI nativa en SwiftUI con Liquid Glass, con mínimo iOS 26 y sin apariencia alternativa para versiones anteriores.
- Se respetarán los ajustes de accesibilidad que reduzcan movimiento y transparencia, aunque se limiten los efectos visuales. La legibilidad prevalece sobre el efecto de cristal.

## Idioma

La persona podrá elegir **Sistema**, **Español** o **English**. Con Sistema, la aplicación seguirá el idioma del dispositivo; si este no está soportado, usará inglés. Todo texto visible, incluidos avisos y etiquetas de accesibilidad, estará traducido.
