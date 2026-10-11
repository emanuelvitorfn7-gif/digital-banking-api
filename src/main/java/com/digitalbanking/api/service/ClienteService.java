package com.digitalbanking.api.service;

import com.digitalbanking.api.dto.cliente.ClienteRequest;
import com.digitalbanking.api.dto.cliente.ClienteResponse;
import com.digitalbanking.api.entity.Cliente;
import com.digitalbanking.api.exception.DuplicateResourceException;
import com.digitalbanking.api.exception.InvalidCpfException;
import com.digitalbanking.api.exception.ResourceNotFoundException;
import com.digitalbanking.api.repository.ClienteRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        String cpf = normalizarCpf(request.cpf());
        validarCpf(cpf);
        String email = normalizarEmail(request.email());

        if (clienteRepository.existsByCpf(cpf)) {
            throw new DuplicateResourceException("Já existe um cliente cadastrado com o CPF informado.");
        }
        if (clienteRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Já existe um cliente cadastrado com o e-mail informado.");
        }

        Cliente cliente = new Cliente(request.nome().trim(), cpf, email);
        return ClienteResponse.from(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .map(ClienteResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: id " + id));
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    private void validarCpf(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            throw new InvalidCpfException("CPF deve conter exatamente 11 dígitos numéricos.");
        }
        if (todosDigitosIguais(cpf) || !digitosVerificadoresConferem(cpf)) {
            throw new InvalidCpfException("CPF inválido: dígitos verificadores não conferem.");
        }
    }

    private boolean todosDigitosIguais(String cpf) {
        return cpf.chars().distinct().count() == 1;
    }

    private boolean digitosVerificadoresConferem(String cpf) {
        int[] digitos = new int[11];
        for (int i = 0; i < 11; i++) {
            digitos[i] = cpf.charAt(i) - '0';
        }
        return digitos[9] == calcularDigitoVerificador(digitos, 9)
                && digitos[10] == calcularDigitoVerificador(digitos, 10);
    }

    private int calcularDigitoVerificador(int[] digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += digitos[i] * (quantidade + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

}
