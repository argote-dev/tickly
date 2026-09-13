---
status: accepted
---

# Compartir lógica y mantener interfaces propias de cada plataforma

Tickly compartirá la lógica del temporizador mediante Kotlin Multiplatform y mantendrá interfaces separadas: Compose con Material en Android y SwiftUI con Liquid Glass en iOS. Frente a compartir también la UI, se priorizan la apariencia y las interacciones propias de cada plataforma para un temporizador personal sereno, premium y usable, aceptando mantener y probar dos interfaces.

iOS tendrá un mínimo de versión 26, sin apariencia alternativa para versiones anteriores; Android usará la versión estable más reciente de Material compatible con el proyecto, no versiones preliminares por obtener novedades. La app será exclusivamente oscura y respetará los ajustes de accesibilidad que reduzcan movimiento y transparencia, incluso cuando eso limite los efectos visuales.

Esta decisión fue confirmada durante la entrevista de diseño (Q12–Q15) y está materializada en la implementación actual: Compose Material en Android y SwiftUI Liquid Glass en iOS.
