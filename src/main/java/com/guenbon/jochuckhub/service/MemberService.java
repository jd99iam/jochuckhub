package com.guenbon.jochuckhub.service;

import com.guenbon.jochuckhub.dto.request.MemberCreateDTO;
import com.guenbon.jochuckhub.dto.request.MemberUpdateDTO;
import com.guenbon.jochuckhub.dto.response.MemberResponseDTO;
import com.guenbon.jochuckhub.entity.Member;
import com.guenbon.jochuckhub.exception.NotFoundException;
import com.guenbon.jochuckhub.exception.errorcode.ErrorCode;
import com.guenbon.jochuckhub.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberRepository memberRepository;

    // Create
    @Transactional
    public MemberResponseDTO createMember(MemberCreateDTO createDTO) {
        Member member = new Member();
        member.setUsername(createDTO.getUsername());
        member.setName(createDTO.getName());
        member.setPassword(bCryptPasswordEncoder.encode(createDTO.getPassword()));
        member.setAge(createDTO.getAge());

        if (memberRepository.findByUsername(member.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Member savedMember = memberRepository.save(member);
        return convertToResponseDTO(savedMember);
    }

    // Read - 전체 조회
    public List<MemberResponseDTO> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Read - ID로 조회
    public MemberResponseDTO getMemberById(Long id) {
        return memberRepository.findById(id)
                .map(this::convertToResponseDTO)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // Read - Username으로 조회
    public Optional<MemberResponseDTO> getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .map(this::convertToResponseDTO);
    }

    // Update
    @Transactional
    public MemberResponseDTO updateMember(Long id, MemberUpdateDTO updateDTO) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        if (updateDTO.getUsername() != null) {
            member.setUsername(updateDTO.getUsername());
        }
        if (updateDTO.getName() != null) {
            member.setName(updateDTO.getName());
        }
        if (updateDTO.getPassword() != null) {
            member.setPassword(updateDTO.getPassword());
        }
        if (updateDTO.getAge() != null) {
            member.setAge(updateDTO.getAge());
        }

        Member updatedMember = memberRepository.save(member);
        return convertToResponseDTO(updatedMember);
    }

    // Delete
    @Transactional
    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new NotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }
        memberRepository.deleteById(id);
    }

    // Entity를 ResponseDTO로 변환
    private MemberResponseDTO convertToResponseDTO(Member member) {
        MemberResponseDTO responseDTO = new MemberResponseDTO();
        responseDTO.setId(member.getId());
        responseDTO.setUsername(member.getUsername());
        responseDTO.setName(member.getName());
        responseDTO.setAge(member.getAge());
        return responseDTO;
    }
}

