#!/usr/bin/env python3
"""
Utilidad para generar hashes BCrypt para contraseñas.
Úsalo para crear contraseñas de prueba para la BD.

Requisitos:
pip install bcrypt

Uso:
python3 generate_bcrypt.py "mi_contraseña"

Salida:
$2a$10$... (hash BCrypt)
"""

import bcrypt
import sys

def generate_bcrypt_hash(password):
    """Genera un hash BCrypt para una contraseña."""
    salt = bcrypt.gensalt(rounds=10)
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

def verify_bcrypt_hash(password, hashed_password):
    """Verifica una contraseña contra su hash BCrypt."""
    return bcrypt.checkpw(password.encode('utf-8'), hashed_password.encode('utf-8'))

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python3 generate_bcrypt.py <contraseña>")
        print("\nEjemplo:")
        print('python3 generate_bcrypt.py "mi_password_123"')
        sys.exit(1)
    
    password = sys.argv[1]
    hashed = generate_bcrypt_hash(password)
    
    print(f"Contraseña: {password}")
    print(f"Hash: {hashed}")
    print("\nCopia el hash anterior en la columna 'contrasena' de la tabla 'usuarios'")
