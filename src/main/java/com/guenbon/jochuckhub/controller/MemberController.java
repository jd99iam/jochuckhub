package com.guenbon.jochuckhub.controller;

import com.guenbon.jochuckhub.dto.MemberCreateDTO;
import com.guenbon.jochuckhub.dto.MemberResponseDTO;
import com.guenbon.jochuckhub.dto.MemberUpdateDTO;
import com.guenbon.jochuckhub.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // Create
    @PostMapping
    public ResponseEntity<MemberResponseDTO> createMember(@Valid @RequestBody MemberCreateDTO createDTO) {
        MemberResponseDTO createdMember = memberService.createMember(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMember);
    }

    // Read - 전체 조회
    @GetMapping
    public ResponseEntity<List<MemberResponseDTO>> getAllMembers() {
        List<MemberResponseDTO> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    // Read - ID로 조회
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> updateMember(@PathVariable Long id, @Valid @RequestBody MemberUpdateDTO updateDTO) {
        MemberResponseDTO updatedMember = memberService.updateMember(id, updateDTO);
        return ResponseEntity.ok(updatedMember);
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}

