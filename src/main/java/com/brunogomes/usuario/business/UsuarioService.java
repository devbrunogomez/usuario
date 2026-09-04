package com.brunogomes.usuario.business;

import com.brunogomes.usuario.business.converter.UsuarioConverter;
import com.brunogomes.usuario.business.dto.EnderecoDTO;
import com.brunogomes.usuario.business.dto.TelefoneDTO;
import com.brunogomes.usuario.business.dto.UsuarioDTO;
import com.brunogomes.usuario.infrastructure.entity.Enderecos;
import com.brunogomes.usuario.infrastructure.entity.Telefone;
import com.brunogomes.usuario.infrastructure.entity.Usuario;
import com.brunogomes.usuario.infrastructure.exceptions.ConflictException;
import com.brunogomes.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.brunogomes.usuario.infrastructure.repository.EnderecoRepository;
import com.brunogomes.usuario.infrastructure.repository.TelefoneRepository;
import com.brunogomes.usuario.infrastructure.repository.UsuarioRepository;
import com.brunogomes.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;


    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO) {
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO);
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public void emailExiste(String email) {
        try {
            boolean existe = verificarEmailExistente(email);
            if (existe) {
                throw new ConflictException("Email já cadastrado" + email);
            }
        } catch (ConflictException e) {
            throw new ConflictException("Email já cadastrado" + e.getCause());
        }
    }

    public boolean verificarEmailExistente(String email) {
        return usuarioRepository.existsByEmail(email);


    }

    public UsuarioDTO buscarUsuarioPorEmail(String email) {
        try {

            return usuarioConverter.paraUsuarioDTO(
                    usuarioRepository.findByEmail(email)
                            .orElseThrow(
                            () -> new ResourceNotFoundException("Email não encontrado " + email)
                    )
            );
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Email não encontrado " + email);
        }
    }


    public void deletaUsuarioPorEmail(String email) {
        usuarioRepository.deleteByEmail(email);

    }

    //Aqui buscamos o email do usuário através do toke (tirara obrigatoriedade do e-mail)
    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto) {
        String email = jwtUtil.extraiEmailTOken(token.substring(7));


        // Criptografia de senha
        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);


        // Busca os dados do usuário no banco de dados
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado"));

        //Mesclou os dados que recebemos na requisição DTO com os dados do bando de dados.
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);

        //Salvou os dados do usuário convertido e depois pegou o retorno e converteu para UsuarioDTO
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));


    }

    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO) {

        Enderecos entity = enderecoRepository.findById(idEndereco).orElseThrow(() ->
                new ResourceNotFoundException("Id não encontrado " + idEndereco));

        Enderecos enderecos = usuarioConverter.updateEndereco(enderecoDTO, entity);

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(enderecos));

    }

    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO dto) {

        Telefone entity = telefoneRepository.findById(idTelefone).orElseThrow(() ->
                new ResourceNotFoundException("Id não encontrado " + idTelefone));

        Telefone telefone = usuarioConverter.updateTelefone(dto, entity);
        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));

    }


}
