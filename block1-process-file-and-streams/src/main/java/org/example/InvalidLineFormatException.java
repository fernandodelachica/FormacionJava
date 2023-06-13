package org.example;

public class InvalidLineFormatException extends Exception{
        // Creamos la excepción
        public InvalidLineFormatException(String message) {
            // Devuelve el mensaje de error
            super(message);
        }
}
