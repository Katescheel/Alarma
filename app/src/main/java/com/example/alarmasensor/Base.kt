package com.example.alarmasensor

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class BaseDatos(context: Context) : SQLiteOpenHelper(context, "usuarios.db", null, 1) {


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE usuarios (usuario TEXT PRIMARY KEY, password TEXT)")
    }


    override fun onUpgrade(db: SQLiteDatabase, vieja: Int, nueva: Int) {}


    fun registrar(usuario: String, password: String): Boolean {
        val valores = ContentValues()
        valores.put("usuario", usuario)
        valores.put("password", hash(password))   // nunca guardamos la contraseña real
        return writableDatabase.insert("usuarios", null, valores) != -1L
    }

    // PASO 4: Comprobar el login (SELECT).
    //         El "?"evita inyección SQL
    fun login(usuario: String, password: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM usuarios WHERE usuario = ? AND password = ?",
            arrayOf(usuario, hash(password))
        )
        val existe = cursor.count > 0   // ¿encontró alguna fila?
        cursor.close()
        return existe
    }


    private fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
