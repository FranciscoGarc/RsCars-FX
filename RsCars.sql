USE master
DROP DATABASE IF EXISTS RScars;

CREATE DATABASE RScars
GO

USE RScars
GO

CREATE TABLE tbTipoUsuarios(
	idTipo int identity(1,1) primary key,
	tipo varchar(20)
);

INSERT INTO tbTipoUsuarios(tipo) values ('Mecanico'),('Contador'),('Cliente');

CREATE TABLE tbUsuarios(
	idUsuario int identity(1,1) primary key,
	idTipo int,
	usuario varchar(30) unique,
	contra varchar(60),
	correo varchar(50)
);

ALTER TABLE tbUsuarios ADD CONSTRAINT fk_usuario_tipo
FOREIGN KEY (idTipo) REFERENCES tbTipoUsuarios(idTipo);

CREATE TABLE tbClientes(
	idCliente int identity(1,1) primary key,
	idUsuario int,
	nombre varchar(50),
	apellido varchar(50),
	telefono varchar(20),
	direccion varchar(100),
	dui varchar(10)
);

ALTER TABLE tbClientes ADD CONSTRAINT fk_cliente_usu
FOREIGN KEY (idUsuario) REFERENCES tbUsuarios(idUsuario);

CREATE TABLE tbVehiculos(
	idVehiculo int identity(1,1) primary key,
	marca varchar(50),
	modelo varchar(50),
	año int,
	placa varchar(10),
	idCliente int
);

ALTER TABLE tbVehiculos ADD CONSTRAINT fk_clientes_vehiculos
FOREIGN KEY (idCliente) REFERENCES tbClientes (idCliente);

CREATE TABLE tbServicios(
	idServicio int identity(1,1) primary key,
	descripcion varchar(200),
	costo money
);

CREATE TABLE tbPromociones(
	idPromocion int identity(1,1) primary key,
	descripcion varchar(200),
	descuento int,
	fechaInicio date,
	fechaFin date
);

CREATE TABLE tbContadores (
	idEmpleado int identity (1,1) primary key,
	idUsuario int,
	nombre varchar(50),
	apellido varchar(50),
	telefono varchar(20),
	direccion varchar(100),
	dui varchar(10)
);

ALTER TABLE tbContadores ADD CONSTRAINT fk_contador_usu
FOREIGN KEY (idUsuario) REFERENCES tbUsuarios(idUsuario);

CREATE TABLE tbMecanicos(
	idMecanico int identity(1,1) primary key,
	idUsuario int,
	nombre varchar(50),
	apellido varchar(50),
	telefono varchar(20),
	direccion varchar(100),
	dui varchar(10)
);

ALTER TABLE tbMecanicos ADD CONSTRAINT fk_mcanicos_usu
FOREIGN KEY (idUsuario) REFERENCES tbUsuarios(idUsuario);

CREATE TABLE tbComentarios(
	idComentario int identity(1,1) primary key,
	texto text,
	calificacion int,
	idCliente int,
	idVehiculo int
);

ALTER TABLE tbComentarios ADD CONSTRAINT fk_clientes_comentarios
FOREIGN KEY (idCliente) REFERENCES tbClientes (idCliente);

ALTER TABLE tbComentarios ADD CONSTRAINT fk_vehiculos_comentarios
FOREIGN KEY (idVehiculo) REFERENCES tbVehiculos(idVehiculo);

CREATE TABLE tbProveedores (
	idProveedor int identity (1,1) primary key,
	nombre varchar(50),
	telefono varchar(20),
	correo varchar(55)
);

CREATE TABLE tbRepuestos (
	idRepuesto int identity (1,1) primary key,
	descripcion varchar(50),
	precio int,
	stock int,
	idProveedor int
);

ALTER TABLE tbRepuestos ADD CONSTRAINT fk_proveedores_repuestos
FOREIGN KEY (idProveedor) REFERENCES tbProveedores(idProveedor);

CREATE TABLE tbChat (
	idMensaje int identity (1,1) primary key,
	texto text,
	idCliente int,
	idMecanico int
);

ALTER TABLE tbChat ADD CONSTRAINT fk_clientes_chat
FOREIGN KEY (idCliente) REFERENCES tbClientes (idCliente);

ALTER TABLE tbChat ADD CONSTRAINT fk_mecanicos_chat
FOREIGN KEY (idMecanico) REFERENCES tbMecanicos(idMecanico);

CREATE TABLE tbHistorialVehiculo (
	idHistorial int identity (1,1) primary key,
	fecha date,
	kilometraje varchar(50),
	idVehiculo int
);

ALTER TABLE tbHistorialVehiculo ADD CONSTRAINT fk_vehiculos_historialveh
FOREIGN KEY (idVehiculo) REFERENCES tbVehiculos(idVehiculo);

CREATE TABLE tbAlertas (
	idAlerta int identity (1,1) primary key,
	texto text,
	recordatorio date,
	idVehiculo int
);

ALTER TABLE tbAlertas ADD CONSTRAINT fk_vehiculos_alertas
FOREIGN KEY (idVehiculo) REFERENCES tbVehiculos(idVehiculo);

CREATE TABLE tbCitas (
	idcita int identity(1,1) primary key,
	fechaHora datetime,
	idVehiculo int,
	idServicio int,
	idRepuesto int,
	estado varchar(220),
	idPromocion int
);

ALTER TABLE tbCitas ADD CONSTRAINT fk_vehiculos_citas
FOREIGN KEY (idVehiculo) REFERENCES tbVehiculos(idVehiculo);

ALTER TABLE tbCitas ADD CONSTRAINT fk_servicios_citas
FOREIGN KEY (idServicio) REFERENCES tbServicios(idServicio);

ALTER TABLE tbCitas ADD CONSTRAINT fk_promociones_citas
FOREIGN KEY (idPromocion) REFERENCES tbPromociones(idPromocion);

ALTER TABLE tbCitas ADD CONSTRAINT fk_repuestos_citas
FOREIGN KEY (idRepuesto) REFERENCES tbRepuestos(idRepuesto);

CREATE TABLE tbPagos(
	idPago int identity (1,1) primary key,
	monto int,
	metodo varchar(50),
	idCita int
);

ALTER TABLE tbPagos ADD CONSTRAINT fk_citas_pagos
FOREIGN KEY (idCita) REFERENCES tbCitas(idCita);

CREATE TABLE tbGastos (
	idGasto int identity (1,1) primary key,
	concepto varchar(50),
	monto int,
	fecha date,
	idCita int
);

ALTER TABLE tbGastos ADD CONSTRAINT fk_citas_gastos
FOREIGN KEY (idCita) REFERENCES tbCitas(idCita);

CREATE TABLE tbVentas(
	idVenta int identity (1,1) primary key,
	subtotal money,
	iva int,
	total money,
	idCita int,
	idCliente int,
	idVehiculo int,
	idRepuesto int,
	idServicio int,
	idPago int
);

ALTER TABLE tbVentas ADD CONSTRAINT fk_citas_ventas
FOREIGN KEY (idCita) REFERENCES tbCitas(idCita);

ALTER TABLE tbVentas ADD CONSTRAINT fk_clientes_ventas
FOREIGN KEY (idCliente) REFERENCES tbClientes (idCliente);

ALTER TABLE tbVentas ADD CONSTRAINT fk_vehiculos_ventas
FOREIGN KEY (idVehiculo) REFERENCES tbVehiculos(idVehiculo);

ALTER TABLE tbVentas ADD CONSTRAINT fk_repuestos_ventas
FOREIGN KEY (idRepuesto) REFERENCES tbRepuestos(idRepuesto);

ALTER TABLE tbVentas ADD CONSTRAINT fk_servicios_ventas
FOREIGN KEY (idServicio) REFERENCES tbServicios(idServicio);

ALTER TABLE tbVentas ADD CONSTRAINT fk_pagos_ventas
FOREIGN KEY (idPago) REFERENCES tbPagos(idPago);

USE RScars;

-- Datos de ejemplo más realistas
INSERT INTO tbUsuarios (idTipo, usuario, contra, correo) VALUES
    (1, 'pedro.mecanico', 'Pedro2024!', 'pedro.garcia@tallereselrosal.com'),
    (2, 'carlos.contador', 'Contador#1', 'carlos.lopez@contadores.com'),
    (3, 'juan.perez', 'Cliente123', 'juan.perez@gmail.com'),
    (3, 'maria.gomez', 'Maria2024', 'maria.gomez@hotmail.com');

INSERT INTO tbClientes (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES
    (3, 'Juan', 'Pérez', '78451236', 'Colonia Escalón, San Salvador', '01234567-8'),
    (4, 'María', 'Gómez', '78965412', 'Residencial Altavista, Soyapango', '12345678-9');

INSERT INTO tbVehiculos (marca, modelo, año, placa, idCliente) VALUES
    ('Toyota', 'Corolla', 2020, 'P123456', 1),
    ('Honda', 'Civic', 2018, 'P654321', 2);

INSERT INTO tbServicios (descripcion, costo) VALUES
    ('Cambio de aceite y filtro', 55.00),
    ('Revisión y ajuste de frenos', 80.00);

INSERT INTO tbPromociones (descripcion, descuento, fechaInicio, fechaFin) VALUES
    ('Descuento verano 2023', 10, '2023-06-01', '2023-06-30'),
    ('Promo invierno 2023', 15, '2023-12-01', '2023-12-31');

INSERT INTO tbContadores (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES
    (2, 'Carlos', 'López', '22547896', 'Av. Las Magnolias, San Salvador', '23456789-0');

INSERT INTO tbMecanicos (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES
    (1, 'Pedro', 'García', '78965432', 'Calle El Progreso, Santa Tecla', '34567890-1');

INSERT INTO tbComentarios (texto, calificacion, idCliente, idVehiculo) VALUES
    ('Excelente atención y servicio rápido.', 5, 1, 1),
    ('El trabajo fue bueno, pero tardaron más de lo esperado.', 3, 2, 2);

INSERT INTO tbProveedores (nombre, telefono, correo) VALUES
    ('Repuestos El Salvador', '22223333', 'ventas@repuestoselsalvador.com'),
    ('AutoPartes S.A.', '22334455', 'contacto@autopartes.com.sv');

INSERT INTO tbRepuestos (descripcion, precio, stock, idProveedor) VALUES
    ('Batería Bosch 12V', 110, 30, 1),
    ('Filtro de aceite Mann', 15, 80, 2);

INSERT INTO tbChat (texto, idCliente, idMecanico) VALUES
    ('Buenos días, mi carro hace un ruido extraño.', 1, 1),
    ('¿Puede describir el ruido o cuándo ocurre?', 1, 1);

INSERT INTO tbHistorialVehiculo (fecha, kilometraje, idVehiculo) VALUES
    ('2023-01-01', '5,000 km', 1),
    ('2023-02-15', '8,200 km', 2);

INSERT INTO tbAlertas (texto, recordatorio, idVehiculo) VALUES
    ('Cambio de aceite programado', '2023-04-01', 1),
    ('Revisión de frenos pendiente', '2023-05-15', 2);

INSERT INTO tbCitas (fechaHora, idVehiculo, idServicio, idRepuesto, estado, idPromocion) VALUES
    ('2023-03-20 09:00:00', 1, 1, 1, 'Pendiente', NULL),
    ('2023-03-21 14:30:00', 2, 2, NULL, 'Pendiente', 2);

INSERT INTO tbPagos (monto, metodo, idCita) VALUES
    (55, 'Efectivo', 1),
    (80, 'Tarjeta de crédito', 2);

INSERT INTO tbGastos (concepto, monto, fecha, idCita) VALUES
    ('Compra de repuestos', 120, '2023-03-20', 1),
    ('Mano de obra', 80, '2023-03-21', 2);

INSERT INTO tbVentas (subtotal, iva, total, idCita, idCliente, idVehiculo, idRepuesto, idServicio, idPago) VALUES
    (110.00, 13, 123.00, 1, 1, 1, 1, 1, 1),
    (80.00, 10, 90.00, 2, 2, 2, NULL, 2, 2);

SELECT * FROM tbClientes;