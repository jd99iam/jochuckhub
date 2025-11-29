package com.guenbon.jochuckhub.controller;

import com.guenbon.jochuckhub.dto.response.TeamResponseDTO;
import com.guenbon.jochuckhub.entity.Team;
import com.guenbon.jochuckhub.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponseDTO> createTeam(@RequestParam String teamName) {
        TeamResponseDTO createdTeamDto = teamService.createTeam(teamName);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeamDto);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponseDTO> getTeamById(@PathVariable Long teamId) {
        TeamResponseDTO teamResponseDTO = teamService.findTeamById(teamId);
        return ResponseEntity.ok(teamResponseDTO);
    }
}
