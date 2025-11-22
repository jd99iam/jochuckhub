package com.guenbon.jochuckhub.controller;

import com.guenbon.jochuckhub.dto.request.MemberCreateDTO;
import com.guenbon.jochuckhub.dto.response.MemberResponseDTO;
import com.guenbon.jochuckhub.dto.request.MemberUpdateDTO;
import com.guenbon.jochuckhub.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입, 모든 권한
    @PostMapping
    public ResponseEntity<MemberResponseDTO> createMember(@Valid @RequestBody MemberCreateDTO createDTO) {
        MemberResponseDTO createdMember = memberService.createMember(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMember);
    }

    // 전체 조회, 관리자 권한
    @GetMapping
    public ResponseEntity<List<MemberResponseDTO>> getAllMembers() {
        List<MemberResponseDTO> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    // id로 조회, 모든 권한
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    // 수정, 회원 권한
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> updateMember(@PathVariable Long id, @Valid @RequestBody MemberUpdateDTO updateDTO) {
        MemberResponseDTO updatedMember = memberService.updateMember(id, updateDTO);
        return ResponseEntity.ok(updatedMember);
    }

    // 삭제, 회원 권한
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}

