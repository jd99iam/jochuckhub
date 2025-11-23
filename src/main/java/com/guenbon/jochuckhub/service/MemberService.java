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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MemberRepository memberRepository;
    private final FileStorageService fileStorageService;   // 새로 추가됨

    // Create
    @Transactional
    public MemberResponseDTO createMember(MemberCreateDTO createDTO, MultipartFile image) throws IOException {

        if (memberRepository.findByUsername(createDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Member member = new Member();
        member.setUsername(createDTO.getUsername());
        member.setName(createDTO.getName());
        member.setPassword(bCryptPasswordEncoder.encode(createDTO.getPassword()));
        member.setAge(createDTO.getAge());

        // 이미지 업로드 처리 전에 먼저 Member 생성하는 이유는 이미지 경로에 memberId를 사용할 것이기 때문
        Member savedMember = memberRepository.save(member);

        // 이미지 업로드 처리
        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.saveProfileImage(savedMember.getId(), image);
            savedMember.setProfileImageUrl(imageUrl);   // Entity에 필드 있어야 함
        } else {
            // static/images/default-profile.png 에 존재해야 함
            savedMember.setProfileImageUrl("/images/default-profile.png");
        }

        return MemberResponseDTO.of(savedMember);
    }

    // Read - 전체 조회
    public List<MemberResponseDTO> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberResponseDTO::of)
                .collect(Collectors.toList());
    }

    // Read - ID로 조회
    public MemberResponseDTO getMemberById(Long id) {
        return memberRepository.findById(id)
                .map(MemberResponseDTO::of)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // Read - Username으로 조회
    public MemberResponseDTO getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .map(MemberResponseDTO::of)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
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
        return MemberResponseDTO.of(updatedMember);
    }

    // Delete
    @Transactional
    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new NotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }
        memberRepository.deleteById(id);
    }
}

