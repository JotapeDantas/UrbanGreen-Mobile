package com.example.myapplicationandroid

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class TelaLoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var senhaEditText: EditText
    private lateinit var mostrarSenhaCheckBox: CheckBox
    private lateinit var loginButton: Button
    private lateinit var esqueceuSenhaTextView: TextView

    private val nomeBanco = "fazenda_urbana_Urban_Green_pim4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_login)

        emailEditText = findViewById(R.id.email)
        senhaEditText = findViewById(R.id.senha)
        mostrarSenhaCheckBox = findViewById(R.id.checkbox)
        loginButton = findViewById(R.id.btnLogin)
        esqueceuSenhaTextView = findViewById(R.id.esqueceusenha)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val senha = senhaEditText.text.toString()

            // Validação simples
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validação do email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                (!email.endsWith("@hotmail.com") &&
                        !email.endsWith("@yahoo.com") &&
                        !email.endsWith("@gmail.com"))) {
                Toast.makeText(this, "Por favor, insira um email válido e que seja do Hotmail, Yahoo ou Gmail.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            autenticarUsuario(email, senha)
        }

        mostrarSenhaCheckBox.setOnCheckedChangeListener { _, isChecked ->
            senhaEditText.inputType = if (isChecked) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            senhaEditText.setSelection(senhaEditText.text.length)
        }

        esqueceuSenhaTextView.setOnClickListener {
            val intent = Intent(this, AlterarSenhaActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun autenticarUsuario(email: String, senha: String) {
        val connectionString = "jdbc:sqlserver://NOTEBOOK-LUCAS\\SQL;databaseName=$nomeBanco;integratedSecurity=true;"
        val loginQuery = """
            SELECT u.cod_usuario, c.nome_cargo
            FROM usuario u
            INNER JOIN funcionario f ON u.cod_funcionario = f.cod_funcionario
            INNER JOIN cargo c ON f.cod_cargo = c.cod_cargo
            WHERE u.email = ? AND u.senha = ?
        """.trimIndent()

        Thread {
            var connection: Connection? = null
            var preparedStatement: PreparedStatement? = null
            var resultSet: ResultSet? = null

            try {
                connection = DriverManager.getConnection(connectionString)
                preparedStatement = connection.prepareStatement(loginQuery)
                preparedStatement.setString(1, email)
                preparedStatement.setString(2, senha)

                resultSet = preparedStatement.executeQuery()

                if (resultSet.next()) {
                    val nomeCargo = resultSet.getString("nome_cargo")

                    runOnUiThread {
                        Toast.makeText(this, "Acesso permitido como $nomeCargo.", Toast.LENGTH_SHORT).show()
                        // Aqui você pode iniciar a próxima Activity dependendo do cargo
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show()
                        emailEditText.setText("")
                        senhaEditText.setText("")
                        emailEditText.requestFocus()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Erro ao acessar o banco de dados: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                try {
                    resultSet?.close()
                    preparedStatement?.close()
                    connection?.close()
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "Erro ao fechar recursos: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()
    }
}
