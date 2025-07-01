USE master
Drop Database RScars;


CREATE DATABASE RScars
GO

USE RScars
GO

CREATE TABLE tbTipoUsuarios(
	idTipo int identity(1,1) primary key,
	tipo char(20)
);

INSERT INTO tbTipoUsuarios(tipo) values ('Mecanico'),('Contador'),('Cliente');

CREATE TABLE tbUsuarios(
	idUsuario int identity(1,1) primary key,
	idTipo int,
	usuario varchar(30) unique,
	contra varchar(20),
	correo varchar(50)
);

alter table tbUsuarios add constraint fk_usuario_tipo
foreign key (idTipo) references tbTipoUsuarios(idTipo);


CREATE TABLE tbClientes(
	idCliente int identity(1,1) primary key,
	idUsuario int,
	nombre char(50),
	apellido char(50),
	telefono char(50),
	direccion char(50),
	dui char (10)
);

ALTER TABLE tbClientes ADD CONSTRAINT fk_cliente_usu
FOREIGN KEY (idUsuario) REFERENCES tbUsuarios(idUsuario);

CREATE TABLE tbVehiculos(
	idVehiculo int identity(1,1) primary key,
	marca char(50),
	modelo char(50),
	año int,
	placa char(50),
	idCliente int
);

ALTER TABLE tbVehiculos ADD CONSTRAINT fk_clientes_vehiculos
FOREIGN KEY (idCliente) REFERENCES tbClientes (idCliente);

CREATE TABLE tbServicios(
	idServicio int identity(1,1) primary key,
	descripcion char(200),
	costo money	
);

Create table  tbPromociones(
 idPromocion int identity(1,1) primary key,
 descripción char(200),
 descuento int,
 fechaInicio date,
 fechaFin date
 );



 Create table tbContadores (
	idEmpleado int identity (1,1) primary key,
	idUsuario int,
	nombre char(50),
	apellido char(50),
	telefono char(50),
	direccion char(50),
	dui char (10)
 );

ALTER TABLE tbContadores ADD CONSTRAINT fk_contador_usu
FOREIGN KEY (idUsuario) REFERENCES tbUsuarios(idUsuario);

Create table tbMecanicos( 
	idMecanico int identity(1,1) primary key,
	idUsuario int,
	nombre char(50),
	apellido char(50),
	telefono char(50),
	direccion char(50),
	dui char (10)
);

ALTER TABLE tbMecanicos ADD CONSTRAINT fk_mcanicos_usu
FOREIGN KEY (idUsuario) REFERENCES tbUsuarios(idUsuario);

Create table tbComentarios(
idComentario int identity(1,1) primary key,
texto text,
calificación int,
idCliente int,
idVehiculo int
);

ALTER TABLE tbComentarios ADD CONSTRAINT fk_clientes_comentarios
FOREIGN KEY (idCliente) REFERENCES tbClientes (idCliente);

ALTER TABLE tbComentarios ADD CONSTRAINT fk_vehiculos_comentarios
FOREIGN KEY (idVehiculo) REFERENCES tbVehiculos(idVehiculo);



  Create table tbProveedores (
 idProveedor int identity (1,1) primary key, 
 nombre char(50),
 teléfono char(50),
 correo char(55)
 );


Create table tbRepuestos (
 idRepuesto int identity (1,1) primary key,
 descripción char(50), 
 precio int,
 stock int,
 idProveedor int
 );

ALTER TABLE tbRepuestos ADD CONSTRAINT fk_proveedores_repuestos
FOREIGN KEY (idProveedor) REFERENCES tbProveedores(idProveedor);

 
 Create table tbChat (
 idMensaje int identity (1,1) primary key,
 texto text,
 idCliente int,
 idMecanico int
 );

ALTER TABLE tbChat ADD CONSTRAINT fk_clientes_chat
FOREIGN KEY (idCliente) REFERENCES tbClientes (idCliente);

ALTER TABLE tbChat ADD CONSTRAINT fk_mecanicos_chat
FOREIGN KEY (idMecanico) REFERENCES tbMecanicos(idMecanico);


 Create table tbHistorialVehiculo (
 idHistorial int identity (1,1) primary key,
 fecha date, 
 kilometraje char(50),
 idVehiculo int
 );

 ALTER TABLE tbHistorialVehiculo ADD CONSTRAINT fk_vehiculos_historialveh
FOREIGN KEY (idVehiculo) REFERENCES tbVehiculos(idVehiculo);

 Create table tbAlertas (
 idAlerta int identity (1,1) primary key, 
 texto text,
 recordatorio date,
 idVehiculo int
 );

ALTER TABLE tbAlertas ADD CONSTRAINT fk_vehiculos_alertas
FOREIGN KEY (idVehiculo) REFERENCES tbVehiculos(idVehiculo);

CREATE TABLE tbCitas (
	idcita int identity(1,1) primary key,
	fechaHora date,
	idVehiculo int,
    idServicio int,
	idRepuesto int,
	estado char(220),
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

 Create table tbPagos(
 idPago int identity (1,1) primary key,
 monto int,
 método char(50),
 idCita int
 );

 ALTER TABLE tbPagos ADD CONSTRAINT fk_citas_pagos
FOREIGN KEY (idCita) REFERENCES tbCitas(idCita);

 Create table tbGastos (
 idGasto int identity (1,1) primary key,
 concepto char(50),
 monto int,
 fecha date,
 idCita int
 );

ALTER TABLE tbGastos ADD CONSTRAINT fk_citas_gastos
FOREIGN KEY (idCita) REFERENCES tbCitas(idCita);

Create table tbVentas(
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

use RScars;

INSERT INTO tbUsuarios (idTipo, usuario, contra, correo) VALUES
    (1, 'usuario1', 'contraseña1', 'usuario1@example.com'),
    (2, 'usuario2', 'contraseña2', 'usuario2@example.com'),
    (3, 'usuario3', 'contraseña3', 'usuario3@example.com');

INSERT INTO tbClientes (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES
    (3, 'Juan', 'Pérez', '12345678', 'Calle Principal 123', '1234567890'),
    (3, 'María', 'Gómez', '98765432', 'Avenida Central 456', '0987654321');

INSERT INTO tbVehiculos (marca, modelo, año, placa, idCliente) VALUES
    ('Toyota', 'Corolla', 2020, 'ABC123', 1),
    ('Honda', 'Civic', 2018, 'DEF456', 2);

INSERT INTO tbServicios (descripcion, costo) VALUES
    ('Cambio de aceite', 50.00),
    ('Revisión de frenos', 75.00);

INSERT INTO tbPromociones (descripción, descuento, fechaInicio, fechaFin) VALUES
    ('Descuento de verano', 10, '2023-06-01', '2023-06-30'),
    ('Promoción de invierno', 15, '2023-12-01', '2023-12-31');

INSERT INTO tbContadores (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES
    (2, 'Carlos', 'López', '11111111', 'Avenida Principal 789', '1111111111');

INSERT INTO tbMecanicos (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES
    (1, 'Pedro', 'García', '22222222', 'Calle Secundaria 456', '2222222222');

INSERT INTO tbComentarios (texto, calificación, idCliente, idVehiculo) VALUES
    ('Buen servicio. Recomendado.', 5, 1, 1),
    ('Tiempo de espera muy largo.', 2, 2, 2);

INSERT INTO tbProveedores (nombre, teléfono, correo) VALUES
    ('Proveedor A', '55555555', 'proveedorA@example.com'),
    ('Proveedor B', '66666666', 'proveedorB@example.com');

INSERT INTO tbRepuestos (descripción, precio, stock, idProveedor) VALUES
    ('Batería', 100.00, 50, 1),
    ('Filtro de aceite', 10.00, 100, 2);

INSERT INTO tbChat (texto, idCliente, idMecanico) VALUES
    ('Hola, tengo un problema con mi vehículo.', 1, 1),
    ('¿Cuál es el problema exactamente?', 1, 1);

INSERT INTO tbHistorialVehiculo (fecha, kilometraje, idVehiculo) VALUES
    ('2023-01-01', '5000 km', 1),
    ('2023-02-15', '8000 km', 2);

INSERT INTO tbAlertas (texto, recordatorio, idVehiculo) VALUES
    ('Próximo cambio de aceite', '2023-04-01', 1),
    ('Revisión de frenos pendiente', '2023-05-15', 2);

INSERT INTO tbCitas (fechaHora, idVehiculo, idServicio, idRepuesto, estado, idPromocion) VALUES
    ('2023-03-20 09:00:00', 1, 1, 1, 'Pendiente', NULL),
    ('2023-03-21 14:30:00', 2, 2, NULL, 'Pendiente', 2);

INSERT INTO tbPagos (monto, método, idCita) VALUES
    (50, 'Efectivo', 1),
    (100, 'Tarjeta de crédito', 2);

INSERT INTO tbGastos (concepto, monto, fecha, idCita) VALUES
    ('Materiales de repuesto', 200, '2023-03-20', 1),
    ('Mantenimiento general', 150, '2023-03-21', 2);

INSERT INTO tbVentas (subtotal, iva, total, idCita, idCliente, idVehiculo, idRepuesto, idServicio, idPago) VALUES
    (100.00, 13, 113.00, 1, 1, 1, 1, 1, 1),
    (75.00, 10, 85.00, 2, 2, 2, NULL, 2, 2);


