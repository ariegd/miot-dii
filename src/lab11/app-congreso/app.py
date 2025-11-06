import os
import psycopg2
from flask import Flask, request, jsonify

from flask_cors import CORS #nuevo

app = Flask(__name__)
CORS(app)  # ✅ permite peticiones desde apps locales o Cordova

def conectar_bd():
    return psycopg2.connect(
        host=os.environ.get("DB_HOST", "localhost"),
        dbname=os.environ.get("DB_NAME", "congresodb"),
        user=os.environ.get("DB_USER", "organizador"),
        password=os.environ.get("DB_PASSWORD", "clave123")
    )

@app.route("/asistentes", methods=["GET"])
def listar_asistentes():
    conn = conectar_bd()
    cur = conn.cursor()
    cur.execute("SELECT nombre, email FROM asistentes")
    asistentes = [{"nombre": n, "email": e} for n, e in cur.fetchall()]
    cur.close()
    conn.close()
    return jsonify(asistentes)

@app.route("/asistentes", methods=["POST"])
def registrar_asistente():
    datos = request.get_json()
    conn = conectar_bd()
    cur = conn.cursor()
    cur.execute(
        "INSERT INTO asistentes (nombre, email) VALUES (%s, %s)",
        (datos["nombre"], datos["email"])
    )
    conn.commit()
    cur.close()
    conn.close()
    return {"mensaje": "Asistente registrado"}, 201

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
