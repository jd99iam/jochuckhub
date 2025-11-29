package com.guenbon.jochuckhub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guenbon.jochuckhub.dto.response.TeamResponseDTO;
import com.guenbon.jochuckhub.entity.Member;
import com.guenbon.jochuckhub.entity.MemberTeam;
import com.guenbon.jochuckhub.entity.Team;
import com.guenbon.jochuckhub.entity.TeamRole;
import com.guenbon.jochuckhub.exception.NotFoundException;
import com.guenbon.jochuckhub.exception.errorcode.ErrorCode;
import com.guenbon.jochuckhub.repository.MemberRepository;
import com.guenbon.jochuckhub.repository.MemberTeamRepository;
import com.guenbon.jochuckhub.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper; // ObjectMapper 주입

    @Transactional
    public TeamResponseDTO createTeam(String teamName) {
        // 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 사용자 엔티티 찾기
        Optional<Member> optionalMember = memberRepository.findByUsername(username);
        if (optionalMember.isEmpty()) {
            throw new RuntimeException("로그인된 사용자를 찾을 수 없습니다."); // 적절한 예외 처리 필요
        }
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));


        // Team 생성
        Team team = new Team();
        team.setName(teamName);
        Team savedTeam = teamRepository.save(team);

        // MemberTeam 관계 설정 (현재 사용자를 MANAGER로)
        MemberTeam memberTeam = new MemberTeam();
        memberTeam.setMember(member);
        memberTeam.setTeam(savedTeam);
        memberTeam.setRole(TeamRole.MANAGER);
        MemberTeam savedMemberTeams = memberTeamRepository.save(memberTeam);
        savedTeam.getMemberTeams().add(savedMemberTeams);

        // TeamResponseDTO 생성 및 반환
        TeamResponseDTO teamResponseDTO = buildTeamResponseDTO(savedTeam);

        return teamResponseDTO;
    }

    public TeamResponseDTO findTeamById(Long teamId) {
        String redisKey = "team:" + teamId;
        String cachedTeam = redisTemplate.opsForValue().get(redisKey);

        if (cachedTeam != null) {
            try {
                log.info("팀 조회 cache hit : {}", teamId);
                return objectMapper.readValue(cachedTeam, TeamResponseDTO.class);
            } catch (JsonProcessingException e) {
                // 로깅 또는 예외 처리
                e.printStackTrace();
            }
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TEAM_NOT_FOUND));

        TeamResponseDTO teamResponseDTO = buildTeamResponseDTO(team);

        // Redis에 캐싱 (예: 1시간 유효)
        try {
            String teamResponseJson = objectMapper.writeValueAsString(teamResponseDTO);
            redisTemplate.opsForValue().set(redisKey, teamResponseJson, 1, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            // 로깅 또는 예외 처리
            e.printStackTrace();
        }

        return teamResponseDTO;
    }

    private TeamResponseDTO buildTeamResponseDTO(Team team) {
        List<TeamResponseDTO.TeamMemberDTO> members = team.getMemberTeams().stream()
                .map(mt -> TeamResponseDTO.TeamMemberDTO.builder()
                        .memberId(mt.getMember().getId())
                        .username(mt.getMember().getUsername())
                        .role(mt.getRole())
                        .build())
                .sorted(Comparator.comparing(dto -> {
                    if (dto.getRole() == TeamRole.MANAGER) return 0;
                    if (dto.getRole() == TeamRole.COACH) return 1;
                    if (dto.getRole() == TeamRole.PLAYER) return 2;
                    return 3;
                }))
                .collect(Collectors.toList());

        return TeamResponseDTO.builder()
                .teamName(team.getName())
                .members(members)
                .build();
    }
}
