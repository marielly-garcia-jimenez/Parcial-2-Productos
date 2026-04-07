# 📦 Microservicio de Productos - Gestión de Catálogo

Este servicio se encarga de la persistencia y gestión de todos los artículos del catálogo de la tienda. Es la base de datos de productos de todo el ecosistema.

---

## 🛠️ Stack Tecnológico
- **Base de Datos:** MongoDB (Colección `productos`).
- **Framework:** Spring Boot 3.2.5.
- **Registro:** Cliente de Eureka Service.
- **Observabilidad:** Envío de logs a **CloudWatch** (LocalStack).

---

## 📋 Endpoints Principales (vía Gateway: 8080)
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `/productos` | Lista todos los productos. |
| `GET` | `/productos/{id}` | Busca un producto por su ID único. |
| `POST` | `/productos` | Registra un nuevo producto. |
| `PUT` | `/productos/{id}` | Actualiza información de un producto. |
| `DELETE` | `/productos/{id}` | Elimina un producto del catálogo. |

---

## 🗄️ Modelo de Datos (MongoDB)
Cada producto cuenta con:
- `id`: Identificador único generado por MongoDB.
- `nombre`: Nombre comercial del producto.
- `descripcion`: Detalle del artículo.
- `precio`: Valor monetario.

---

## 🔗 Ecosistema Completo
Para entender cómo interactúa este servicio con el resto de la red, visita el repositorio de [Infraestructura y Guías](https://github.com/marielly-garcia-jimenez/Infraestructura-Examen).

---
<p align="center"> Servicio Core de Microservicios - 2026 </p>
