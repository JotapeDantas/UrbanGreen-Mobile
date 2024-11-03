package tela_de_logins

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class BancoDeDados(private val nomeBanco: String) {
    private val connectionString = "jdbc:sqlserver://NOTEBOOK-LUCAS\\SQL;integratedSecurity=true;"

    // Método para criar o banco de dados se não existir
    fun criarBancoDeDadosSeNaoExistir() {
        val createDbQuery = "IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = ?) " +
                "BEGIN " +
                "CREATE DATABASE ? " +
                "END"

        try {
            DriverManager.getConnection(connectionString + "databaseName=master;").use { con ->
                con.prepareStatement(createDbQuery).use { stmt ->
                    stmt.setString(1, nomeBanco)
                    stmt.setString(2, nomeBanco)
                    stmt.executeUpdate()
                    println("Banco de dados '$nomeBanco' criado ou já existe.")
                }
            }
        } catch (sqlEx: SQLException) {
            throw RuntimeException("Erro ao criar o banco de dados: ${sqlEx.message}", sqlEx)
        }
    }

    // Método para conectar ao banco de dados
    fun conectar(): Connection {
        val connString = "$connectionString;databaseName=$nomeBanco;"
        return DriverManager.getConnection(connString)
    }

    // Método para verificar se o banco de dados existe
    fun verificaBancoDeDadosExistente(): Boolean {
        val query = "SELECT database_id FROM sys.databases WHERE name = ?"

        try {
            DriverManager.getConnection(connectionString + "databaseName=master;").use { con ->
                con.prepareStatement(query).use { stmt ->
                    stmt.setString(1, nomeBanco)
                    val rs = stmt.executeQuery()
                    return rs.next() // Retorna true se o banco de dados existir
                }
            }
        } catch (ex: SQLException) {
            throw RuntimeException("Erro ao verificar o banco de dados: ${ex.message}", ex)
        }
    }

    // Método para criar tabelas se não existirem
    fun criarTabelasSeNaoExistirem() {
        val checkDbQuery = "IF EXISTS (SELECT * FROM sys.databases WHERE name = ?) " +
                "BEGIN " +
                "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'usuario') " +
                "BEGIN " +
                "CREATE TABLE usuario (" +
                "email VARCHAR(100) PRIMARY KEY, " +
                "senha VARCHAR(100)); " +
                "END " +
                "END"

        try {
            conectar().use { con ->
                con.prepareStatement(checkDbQuery).use { stmt ->
                    stmt.setString(1, nomeBanco)
                    stmt.executeUpdate()
                    println("Tabelas criadas ou já existem.")
                }
            }
        } catch (sqlEx: SQLException) {
            throw RuntimeException("Erro ao criar as tabelas: ${sqlEx.message}", sqlEx)
        }
    }
}
