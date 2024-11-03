package com.example.myapplicationandroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AlterarSenhaActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextDdi;
    private EditText editTextDdd;
    private EditText editTextNumero;
    private Button buttonGerarCodigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.alterar_senha);

        // Referenciando os elementos da UI
        editTextEmail = findViewById(R.id.email);
        editTextDdi = findViewById(R.id.numerodd);
        editTextDdd = findViewById(R.id.numeroSP);
        editTextNumero = findViewById(R.id.numero);
        buttonGerarCodigo = findViewById(R.id.btngerarcodigo);

        // Definindo limites de caracteres
        editTextDdi.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        editTextDdd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        editTextNumero.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});

        // Configurando o listener para o botão de gerar código
        buttonGerarCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (gerarCodigo()) {
                        openNextActivity(CriarNovaSenhaActivity.class);
                    } else {
                        // Mensagem caso a geração do código falhe
                        Toast.makeText(AlterarSenhaActivity.this, "Falha ao gerar o código. Tente novamente.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    // Tratamento de erro
                    Toast.makeText(AlterarSenhaActivity.this, "Erro ao gerar o código: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.redefinirsenha), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean gerarCodigo() {
        String email = editTextEmail.getText().toString().trim();
        String ddi = editTextDdi.getText().toString().trim();
        String ddd = editTextDdd.getText().toString().trim();
        String numero = editTextNumero.getText().toString().trim();

        // Validação simples
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(ddi) || TextUtils.isEmpty(ddd) || TextUtils.isEmpty(numero)) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return false;
        }

// Validação do email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                (!email.endsWith("@hotmail.com") &&
                        !email.endsWith("@yahoo.com") &&
                        !email.endsWith("@gmail.com"))) {
            Toast.makeText(this, "Por favor, insira um email válido e que seja do Hotmail, Yahoo ou Gmail.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validação dos números
        if (!isNumeroValido(ddi) || !isNumeroValido(ddd) || !isNumeroValido(numero)) {
            Toast.makeText(this, "Por favor, insira números válidos.", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            enviarCodigoPorWhatsApp(email, ddi, ddd, numero);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao enviar código: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isNumeroValido(String numero) {
        // Verifica se o número é composto apenas por dígitos e tem o tamanho esperado
        return numero.matches("\\d+"); // Altere para o tamanho apropriado, se necessário
    }

    private void enviarCodigoPorWhatsApp(String email, String ddi, String ddd, String numero) {
        String codigoRecuperacao = gerarCodigoAleatorio();

        // Simulando o envio
        Toast.makeText(this, "Código enviado para o WhatsApp: " + codigoRecuperacao, Toast.LENGTH_SHORT).show();
    }

    private String gerarCodigoAleatorio() {
        int codigo = (int) (Math.random() * 900000) + 100000; // Gera um código de 6 dígitos
        return String.valueOf(codigo);
    }

    private void openNextActivity(Class<?> activityClass) {
        Intent intent = new Intent(AlterarSenhaActivity.this, activityClass);
        startActivity(intent);
    }
}
