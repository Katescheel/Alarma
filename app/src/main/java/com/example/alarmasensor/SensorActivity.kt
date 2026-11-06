package com.example.alarmasensor

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID


// Quita el aviso de Android Studio sobre permisos: los pedimos en el PASO 3.
@SuppressLint("MissingPermission")
class SensorActivity : AppCompatActivity() {

    private var socket: BluetoothSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // PASO 2: Mostramos el diseño de res/layout/activity_led.xml
        setContentView(R.layout.activity_fun)
        val estado = findViewById<TextView>(R.id.EstadoConexion)

        // PASO 3: Desde Android 12 hay que pedir permiso para usar Bluetooth.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
        }

        // Botón Conectar
        findViewById<Button>(R.id.btnconectar).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
                estado.text = "Falta permiso de Bluetooth"
                return@setOnClickListener
            }
            estado.text = "Conectando..."
            Thread {
                val resultado = try {
                    conectar()
                    "Conectado al HC-05"
                } catch (e: Exception) {
                    "Error: ${e.message}"
                }
                // Solo el hilo principal puede cambiar la pantalla.
                runOnUiThread { estado.text = resultado }
            }.start()
        }
        findViewById<Button>(R.id.btnDesconectar).setOnClickListener {
            desconectar()
            estado.text = "Desconectado"
            Toast.makeText(this, "Bluetooth desconectado", Toast.LENGTH_SHORT).show()
        }

        // PASO 5: Botones del LED. Enviamos "1" para encender y "0" para apagar.
        findViewById<Button>(R.id.btnPrenderAlarma).setOnClickListener { enviar("1") }
        findViewById<Button>(R.id.btnApagarAlarma).setOnClickListener { enviar("0") }
    }

    // PASO 6: Conectarse al HC-05 (antes hay que emparejarlo en los ajustes del teléfono).
    private fun conectar() {
        val adaptador = getSystemService(BluetoothManager::class.java).adapter
        val hc05 = adaptador.bondedDevices.firstOrNull { it.name == "HC-06" }
            ?: throw Exception("HC-05 no está emparejado")

        // UUID estándar del "puerto serie por Bluetooth" (SPP). Es siempre el mismo.
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val nuevoSocket = hc05.createRfcommSocketToServiceRecord(uuid)
        nuevoSocket.connect()
        socket = nuevoSocket
    }

    // NUEVA FUNCIÓN: Desconectar el HC-05 de forma segura
    private fun desconectar() {
        try {
            socket?.let {
                if (it.isConnected) {
                    it.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            socket = null // Limpiamos la referencia del socket
        }
    }

    // PASO 7: Enviar un dato al Arduino escribiendo en el socket.
    private fun enviar(dato: String) {
        try {
            if (socket == null || !socket!!.isConnected) {
                throw Exception("No hay conexión activa")
            }
            socket!!.outputStream.write(dato.toByteArray())
        } catch (e: Exception) {
            Toast.makeText(this, "Primero conecta con el HC-05", Toast.LENGTH_SHORT).show()
        }
    }

    // PASO 8: Al salir de la pantalla cerramos la conexión.
    override fun onDestroy() {
        super.onDestroy()
        desconectar() // Reutilizamos la función segura de desconexión
    }
}