package com.example.myapplicationandroid;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.regex.Pattern;

import tela_de_logins.BancoDeDados;

public class CriarNovaSenhaActivity extends AppCompatActivity {

    private String codigoVerificacao; // Código de verificação
    private String email; // E-mail do usuário (defina este valor antes de usar)
    private long tempoGeracaoCodigo; // Tempo da geração do código
    private BancoDeDados bancoDeDados; // Referência ao banco de dados

    private EditText inputCodAcess;
    private EditText inputNovaSenha;
    private EditText inputSenhaConfirm;
    private Button buttonEnviar;
    private Button buttonGerarCodigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.criar_nova_senha);

        // Inicialização dos campos
        inputCodAcess = findViewById(R.id.Codigoenviado);
        inputNovaSenha = findViewById(R.id.senhanova);
        inputSenhaConfirm = findViewById(R.id.senha);
        buttonEnviar = findViewById(R.id.btnEnviar);
        buttonGerarCodigo = findViewById(R.id.btnCodigo);

        // Inicialização do banco de dados
        bancoDeDados = new BancoDeDados("fazenda_urbana_Urban_Green_pim4");
        bancoDeDados.criarBancoDeDadosSeNaoExistir();
        bancoDeDados.criarTabelasSeNaoExistirem();

        // Listener para o botão de enviar
        buttonEnviar.setOnClickListener(v -> enviar());

        // Listener para o botão de gerar código
        buttonGerarCodigo.setOnClickListener(v -> gerarNovoCodigo());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void gerarNovoCodigo() {
        Random random = new Random();
        codigoVerificacao = String.valueOf(random.nextInt(900000) + 100000); // Gera um código de 6 dígitos
        tempoGeracaoCodigo = System.currentTimeMillis();

        // Envie o código via WhatsApp ou faça o que for necessário
        Toast.makeText(this, "Código gerado: " + codigoVerificacao, Toast.LENGTH_SHORT).show();
    }

    private boolean codigoValido(String codigoDigitado) {
        return codigoDigitado.equals(codigoVerificacao) && (System.currentTimeMillis() - tempoGeracaoCodigo) < 60000; // 1 minuto
    }

    private boolean validarSenha(String senha) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return Pattern.matches(pattern, senha);
    }

    private boolean senhaDiferente(String novaSenha) {
        String senhaAtual = "";
        String obterSenhaQuery = "SELECT senha FROM usuario WHERE email = ?";

        try (Connection con = bancoDeDados.conectar();
             PreparedStatement stmt = con.prepareStatement(obterSenhaQuery)) {
            stmt.setString(1, email); // Assegure-se de que 'email' está definido corretamente
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                senhaAtual = result.getString("senha");
            }
        } catch (SQLException sqlEx) {
            Toast.makeText(this, "Erro ao acessar o banco de dados: " + sqlEx.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        } catch (Exception ex) {
            Toast.makeText(this, "Erro inesperado: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        return !novaSenha.equals(senhaAtual);
    }

    private boolean atualizarSenha(String novaSenha) {
        String atualizarSenhaQuery = "UPDATE usuario SET senha = ? WHERE email = ?";

        try (Connection con = bancoDeDados.conectar();
             PreparedStatement stmt = con.prepareStatement(atualizarSenhaQuery)) {
            stmt.setString(1, novaSenha);
            stmt.setString(2, email);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException sqlEx) {
            Toast.makeText(this, "Erro ao acessar o banco de dados: " + sqlEx.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        } catch (Exception ex) {
            Toast.makeText(this, "Erro inesperado: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void enviar() {
        String codigoDigitado = inputCodAcess.getText().toString().trim();
        String novaSenha = inputNovaSenha.getText().toString().trim();
        String confirmarSenha = inputSenhaConfirm.getText().toString().trim();

        if (!codigoValido(codigoDigitado)) {
            Toast.makeText(this, "Código de verificação inválido ou expirado. Gere um novo código.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem. Tente novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarSenha(novaSenha)) {
            Toast.makeText(this, "A senha deve ter pelo menos 8 caracteres, incluindo uma letra maiúscula, uma letra minúscula, um número e um caractere especial.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senhaDiferente(novaSenha)) {
            Toast.makeText(this, "A nova senha não pode ser a mesma que a senha atual.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (atualizarSenha(novaSenha)) {
            Toast.makeText(this, "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show();
            // Navegue de volta ou faça o que for necessário
        } else {
            Toast.makeText(this, "Erro ao atualizar a senha. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }
}
