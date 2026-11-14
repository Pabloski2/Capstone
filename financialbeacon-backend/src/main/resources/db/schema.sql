-- =====================================================================
-- Financial Beacon - Esquema de la base de datos relacional
-- Motor: MariaDB 10.4 o superior (XAMPP) o MySQL 8
-- Tablas: users, ingresos_mensuales, gastos, deudas, instrumentos_inversion
-- Vistas: v_resumen_mensual, v_deudas_resumen
-- El script es idempotente: se puede ejecutar muchas veces sin borrar datos.
-- =====================================================================
-- Este archivo lo ejecuta el backend al iniciar (spring.sql.init). La conexión ya apunta a la base.

-- ---------------------------------------------------------------------
-- Tabla users: personas que usan la aplicación
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Identificador del usuario',
  email     VARCHAR(255) NOT NULL COMMENT 'Correo, es único',
  password  VARCHAR(255) NOT NULL COMMENT 'Contraseña (pendiente cifrar con BCrypt)',
  nombre    VARCHAR(255) NOT NULL COMMENT 'Nombre para mostrar',
  PRIMARY KEY (id),
  UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Usuarios de la aplicación';

-- ---------------------------------------------------------------------
-- Tabla ingresos_mensuales: un ingreso por usuario, mes y año
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ingresos_mensuales (
  id       BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Identificador del ingreso',
  monto    DECIMAL(14,2) NOT NULL COMMENT 'Ingreso del mes en pesos',
  mes      INT           NOT NULL COMMENT 'Mes del 1 al 12',
  anio     INT           NOT NULL COMMENT 'Año del ingreso',
  user_id  BIGINT        NOT NULL COMMENT 'Dueño del ingreso',
  PRIMARY KEY (id),
  UNIQUE KEY uq_ingreso_usuario_mes (user_id, mes, anio),
  CONSTRAINT fk_ingresos_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT ck_ingresos_monto CHECK (monto >= 0),
  CONSTRAINT ck_ingresos_mes CHECK (mes BETWEEN 1 AND 12),
  CONSTRAINT ck_ingresos_anio CHECK (anio BETWEEN 2000 AND 2100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Ingreso mensual de cada usuario, actualizable cada mes';

-- ---------------------------------------------------------------------
-- Tabla gastos: gastos clasificados en FIJO, VARIABLE u HORMIGA
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS gastos (
  id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Identificador del gasto',
  descripcion  VARCHAR(255)  NOT NULL COMMENT 'En qué se gastó',
  monto        DECIMAL(14,2) NOT NULL COMMENT 'Monto en pesos',
  categoria    ENUM('FIJO','VARIABLE','HORMIGA') NOT NULL COMMENT 'Tipo de gasto',
  fecha        DATE          NOT NULL COMMENT 'Fecha del gasto',
  user_id      BIGINT        NOT NULL COMMENT 'Dueño del gasto',
  PRIMARY KEY (id),
  KEY idx_gastos_usuario_fecha (user_id, fecha),
  CONSTRAINT fk_gastos_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT ck_gastos_monto CHECK (monto >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Gastos registrados por los usuarios';

-- ---------------------------------------------------------------------
-- Tabla deudas: deudas con su tasa y cuota mínima
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS deudas (
  id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Identificador de la deuda',
  nombre        VARCHAR(255)  NOT NULL COMMENT 'Nombre de la deuda',
  monto_total   DECIMAL(14,2) NOT NULL COMMENT 'Saldo pendiente en pesos',
  tasa_interes  DECIMAL(6,2)  NOT NULL COMMENT 'Tasa de interés anual en porcentaje',
  cuota_minima  DECIMAL(14,2) NOT NULL COMMENT 'Pago mínimo mensual en pesos',
  user_id       BIGINT        NOT NULL COMMENT 'Dueño de la deuda',
  PRIMARY KEY (id),
  KEY idx_deudas_usuario (user_id),
  CONSTRAINT fk_deudas_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT ck_deudas_monto CHECK (monto_total >= 0),
  CONSTRAINT ck_deudas_tasa CHECK (tasa_interes >= 0),
  CONSTRAINT ck_deudas_cuota CHECK (cuota_minima >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deudas registradas por los usuarios';

-- ---------------------------------------------------------------------
-- Tabla instrumentos_inversion: opciones de inversión con datos de referencia
-- Es información educativa, la rentabilidad es un escenario (bajo y alto) y no está garantizada
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS instrumentos_inversion (
  id                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Identificador',
  nombre              VARCHAR(120)  NOT NULL COMMENT 'Nombre de la opción de inversión',
  categoria           VARCHAR(30)   NOT NULL COMMENT 'Tipo de instrumento',
  riesgo              VARCHAR(10)   NOT NULL COMMENT 'BAJO, MEDIO o ALTO',
  rentabilidad_min    DECIMAL(6,2)  NOT NULL COMMENT 'Escenario bajo de rentabilidad anual en porcentaje',
  rentabilidad_max    DECIMAL(6,2)  NOT NULL COMMENT 'Escenario alto de rentabilidad anual en porcentaje',
  plazo_minimo_meses  INT           NOT NULL COMMENT 'Plazo mínimo recomendado en meses',
  monto_minimo        DECIMAL(14,2) NOT NULL COMMENT 'Monto mínimo referencial en pesos',
  liquidez            VARCHAR(120)  NOT NULL COMMENT 'Qué tan rápido se puede recuperar el dinero',
  donde_invertir      VARCHAR(255)  NOT NULL COMMENT 'Dónde se contrata',
  descripcion         VARCHAR(600)  NOT NULL COMMENT 'Qué es',
  consideraciones     VARCHAR(800)  NOT NULL COMMENT 'Riesgos y puntos a tener en cuenta',
  fuente              VARCHAR(400)  NOT NULL COMMENT 'De dónde salen los datos',
  fecha_referencia    DATE          NOT NULL COMMENT 'Fecha de los datos',
  PRIMARY KEY (id),
  UNIQUE KEY uq_instrumento_nombre (nombre),
  CONSTRAINT ck_instr_riesgo CHECK (riesgo IN ('BAJO','MEDIO','ALTO')),
  CONSTRAINT ck_instr_categoria CHECK (categoria IN ('RENTA_FIJA_BANCARIA','FONDOS_MUTUOS','RENTA_VARIABLE','PREVISIONAL','ALTERNATIVO')),
  CONSTRAINT ck_instr_rent CHECK (rentabilidad_min <= rentabilidad_max),
  CONSTRAINT ck_instr_plazo CHECK (plazo_minimo_meses >= 0),
  CONSTRAINT ck_instr_monto CHECK (monto_minimo >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Opciones de inversión de referencia';

-- ---------------------------------------------------------------------
-- Vista v_resumen_mensual: ingreso, gastos por categoría y ahorro estimado de cada mes
-- Es el mismo cálculo que hace la aplicación en el resumen del Dashboard
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_resumen_mensual AS
SELECT
  i.user_id,
  i.anio,
  i.mes,
  i.monto AS ingreso,
  COALESCE(SUM(CASE WHEN g.categoria = 'FIJO'     THEN g.monto END), 0) AS gastos_fijos,
  COALESCE(SUM(CASE WHEN g.categoria = 'VARIABLE' THEN g.monto END), 0) AS gastos_variables,
  COALESCE(SUM(CASE WHEN g.categoria = 'HORMIGA'  THEN g.monto END), 0) AS gastos_hormiga,
  COALESCE(SUM(g.monto), 0) AS total_gastos,
  i.monto - COALESCE(SUM(g.monto), 0) AS ahorro_estimado
FROM ingresos_mensuales i
LEFT JOIN gastos g
  ON g.user_id = i.user_id AND YEAR(g.fecha) = i.anio AND MONTH(g.fecha) = i.mes
GROUP BY i.user_id, i.anio, i.mes, i.monto;

-- ---------------------------------------------------------------------
-- Vista v_deudas_resumen: totales de deuda por usuario (métricas de la pantalla de deudas)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_deudas_resumen AS
SELECT
  user_id,
  COUNT(*) AS cantidad_deudas,
  SUM(monto_total) AS deuda_total,
  SUM(cuota_minima) AS cuotas_minimas,
  ROUND(SUM(monto_total * tasa_interes / 100 / 12), 0) AS interes_mensual_aprox
FROM deudas
GROUP BY user_id;
