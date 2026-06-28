package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.AdminSearchIntentRuleVO;
import com.k12.common.dto.AdminSearchRedirectVO;
import com.k12.common.dto.AdminSearchRedirectWriteDTO;
import com.k12.common.dto.AdminSearchSynonymVO;
import com.k12.common.dto.AdminSearchSynonymWriteDTO;
import com.k12.common.dto.NavTargetDTO;
import com.k12.resource.entity.SysSearchIntentRule;
import com.k12.resource.entity.SysSearchRedirect;
import com.k12.resource.entity.SysSearchSynonym;
import com.k12.resource.mapper.SysSearchIntentRuleMapper;
import com.k12.resource.mapper.SysSearchRedirectMapper;
import com.k12.resource.mapper.SysSearchSynonymMapper;
import com.k12.resource.search.SearchLexiconService;
import com.k12.resource.search.SearchRedirectConstants;
import com.k12.resource.search.SearchRedirectRuleService;
import com.k12.resource.service.AdminSearchLexiconService;
import com.k12.resource.util.HomeNavTargetValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class AdminSearchLexiconServiceImpl implements AdminSearchLexiconService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysSearchSynonymMapper synonymMapper;
    private final SysSearchRedirectMapper redirectMapper;
    private final SysSearchIntentRuleMapper intentRuleMapper;
    private final SearchLexiconService searchLexiconService;
    private final SearchRedirectRuleService redirectRuleService;
    public AdminSearchLexiconServiceImpl(SysSearchSynonymMapper synonymMapper, SysSearchRedirectMapper redirectMapper, SysSearchIntentRuleMapper intentRuleMapper, SearchLexiconService searchLexiconService, SearchRedirectRuleService redirectRuleService) {
        this.synonymMapper = synonymMapper;
        this.redirectMapper = redirectMapper;
        this.intentRuleMapper = intentRuleMapper;
        this.searchLexiconService = searchLexiconService;
        this.redirectRuleService = redirectRuleService;
    }


    @Override
    public List<AdminSearchSynonymVO> listSynonyms(boolean includeDisabled, String domain) {
        LambdaQueryWrapper<SysSearchSynonym> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(SysSearchSynonym::getStatus, 1);
        }
        if (StringUtils.hasText(domain)) {
            wrapper.eq(SysSearchSynonym::getDomain, domain.trim());
        }
        wrapper.orderByAsc(SysSearchSynonym::getDomain).orderByAsc(SysSearchSynonym::getWord);
        return synonymMapper.selectList(wrapper).stream().map(this::toSynonymVo).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminSearchSynonymVO createSynonym(AdminSearchSynonymWriteDTO dto) {
        validateSynonymWrite(dto, null);
        SysSearchSynonym row = new SysSearchSynonym();
        applySynonym(row, dto);
        synonymMapper.insert(row);
        searchLexiconService.refreshFromDb();
        return toSynonymVo(row);
    }

    @Override
    @Transactional
    public AdminSearchSynonymVO updateSynonym(Long id, AdminSearchSynonymWriteDTO dto) {
        SysSearchSynonym row = requireSynonym(id);
        validateSynonymWrite(dto, id);
        applySynonym(row, dto);
        synonymMapper.updateById(row);
        searchLexiconService.refreshFromDb();
        return toSynonymVo(synonymMapper.selectById(id));
    }

    @Override
    @Transactional
    public void setSynonymStatus(Long id, Integer status) {
        SysSearchSynonym row = requireSynonym(id);
        row.setStatus(normalizeStatus(status));
        synonymMapper.updateById(row);
        searchLexiconService.refreshFromDb();
    }

    @Override
    @Transactional
    public void deleteSynonym(Long id) {
        requireSynonym(id);
        synonymMapper.deleteById(id);
        searchLexiconService.refreshFromDb();
    }

    @Override
    @Transactional
    public AdminSearchSynonymVO createSynonymDraft(String keyword) {
        String word = normalizeKeyword(keyword);
        if (!StringUtils.hasText(word)) {
            throw new BusinessException(400, "关键词不能为空");
        }
        SysSearchSynonym existing = synonymMapper.selectOne(
                new LambdaQueryWrapper<SysSearchSynonym>()
                        .eq(SysSearchSynonym::getWord, word)
                        .eq(SysSearchSynonym::getDomain, "global")
                        .last("LIMIT 1"));
        if (existing != null) {
            return toSynonymVo(existing);
        }
        AdminSearchSynonymWriteDTO dto = new AdminSearchSynonymWriteDTO();
        dto.setWord(word);
        dto.setSynonyms(word);
        dto.setDomain("global");
        dto.setCanonical(word);
        dto.setStatus(0);
        return createSynonym(dto);
    }

    @Override
    public List<AdminSearchRedirectVO> listRedirects(boolean includeDisabled) {
        LambdaQueryWrapper<SysSearchRedirect> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(SysSearchRedirect::getStatus, 1);
        }
        wrapper.orderByDesc(SysSearchRedirect::getPriority).orderByAsc(SysSearchRedirect::getKeyword);
        return redirectMapper.selectList(wrapper).stream().map(this::toRedirectVo).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminSearchRedirectVO createRedirect(AdminSearchRedirectWriteDTO dto) {
        validateRedirectWrite(dto, null);
        SysSearchRedirect row = new SysSearchRedirect();
        applyRedirect(row, dto);
        redirectMapper.insert(row);
        redirectRuleService.refreshFromDb();
        return toRedirectVo(row);
    }

    @Override
    @Transactional
    public AdminSearchRedirectVO updateRedirect(Long id, AdminSearchRedirectWriteDTO dto) {
        SysSearchRedirect row = requireRedirect(id);
        validateRedirectWrite(dto, id);
        applyRedirect(row, dto);
        redirectMapper.updateById(row);
        redirectRuleService.refreshFromDb();
        return toRedirectVo(redirectMapper.selectById(id));
    }

    @Override
    @Transactional
    public void setRedirectStatus(Long id, Integer status) {
        SysSearchRedirect row = requireRedirect(id);
        row.setStatus(normalizeStatus(status));
        redirectMapper.updateById(row);
        redirectRuleService.refreshFromDb();
    }

    @Override
    @Transactional
    public void deleteRedirect(Long id) {
        requireRedirect(id);
        redirectMapper.deleteById(id);
        redirectRuleService.refreshFromDb();
    }

    @Override
    public List<AdminSearchIntentRuleVO> listIntentRules(boolean includeDisabled) {
        LambdaQueryWrapper<SysSearchIntentRule> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(SysSearchIntentRule::getStatus, 1);
        }
        wrapper.orderByDesc(SysSearchIntentRule::getPriority).orderByAsc(SysSearchIntentRule::getId);
        return intentRuleMapper.selectList(wrapper).stream().map(this::toIntentRuleVo).collect(Collectors.toList());
    }

    private void validateSynonymWrite(AdminSearchSynonymWriteDTO dto, Long excludeId) {
        String word = requireText(dto.getWord(), "标准词");
        String domain = StringUtils.hasText(dto.getDomain()) ? dto.getDomain().trim() : "global";
        requireText(dto.getSynonyms(), "同义词");
        dto.setWord(word);
        dto.setDomain(domain);

        LambdaQueryWrapper<SysSearchSynonym> wrapper = new LambdaQueryWrapper<SysSearchSynonym>()
                .eq(SysSearchSynonym::getWord, word)
                .eq(SysSearchSynonym::getDomain, domain);
        if (excludeId != null) {
            wrapper.ne(SysSearchSynonym::getId, excludeId);
        }
        Long count = synonymMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, "同 domain 下标准词已存在");
        }
    }

    private void validateRedirectWrite(AdminSearchRedirectWriteDTO dto, Long excludeId) {
        String keyword = normalizeKeyword(dto.getKeyword());
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(400, "关键词不能为空");
        }
        if (SearchRedirectConstants.isVagueSubject(keyword)) {
            throw new BusinessException(400, "泛学科词不可配置搜索直达：" + keyword);
        }
        String routePath = resolveRoutePath(dto);
        if (!StringUtils.hasText(routePath)) {
            throw new BusinessException(400, "请填写 routePath 或 route 类型 navTarget");
        }
        if (!routePath.startsWith("/")) {
            throw new BusinessException(400, "routePath 必须以 / 开头");
        }
        dto.setKeyword(keyword);
        dto.setRoutePath(routePath);

        LambdaQueryWrapper<SysSearchRedirect> wrapper = new LambdaQueryWrapper<SysSearchRedirect>()
                .eq(SysSearchRedirect::getKeyword, keyword);
        if (excludeId != null) {
            wrapper.ne(SysSearchRedirect::getId, excludeId);
        }
        Long count = redirectMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, "关键词重定向已存在");
        }
    }

    private String resolveRoutePath(AdminSearchRedirectWriteDTO dto) {
        if (StringUtils.hasText(dto.getRoutePath())) {
            return dto.getRoutePath().trim();
        }
        NavTargetDTO nav = dto.getNavTarget();
        if (nav != null && "route".equalsIgnoreCase(nav.getType()) && StringUtils.hasText(nav.getRoutePath())) {
            return nav.getRoutePath().trim();
        }
        return null;
    }

    private void applySynonym(SysSearchSynonym row, AdminSearchSynonymWriteDTO dto) {
        row.setWord(dto.getWord().trim());
        row.setSynonyms(dto.getSynonyms().trim());
        row.setDomain(StringUtils.hasText(dto.getDomain()) ? dto.getDomain().trim() : "global");
        row.setCanonical(StringUtils.hasText(dto.getCanonical()) ? dto.getCanonical().trim() : row.getWord());
        row.setStatus(normalizeStatus(dto.getStatus()));
    }

    private void applyRedirect(SysSearchRedirect row, AdminSearchRedirectWriteDTO dto) {
        row.setKeyword(dto.getKeyword().trim());
        row.setTitle(StringUtils.hasText(dto.getTitle()) ? dto.getTitle().trim() : row.getKeyword());
        row.setRoutePath(dto.getRoutePath().trim());
        if (dto.getNavTarget() != null && "route".equalsIgnoreCase(dto.getNavTarget().getType())) {
            NavTargetDTO validated = HomeNavTargetValidator.validate(dto.getNavTarget());
            row.setNavTarget(HomeNavTargetValidator.toJson(validated));
        } else {
            row.setNavTarget(null);
        }
        row.setPriority(dto.getPriority() == null ? 0 : dto.getPriority());
        row.setStatus(normalizeStatus(dto.getStatus()));
        row.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
    }

    private SysSearchSynonym requireSynonym(Long id) {
        SysSearchSynonym row = synonymMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "同义词不存在");
        }
        return row;
    }

    private SysSearchRedirect requireRedirect(Long id) {
        SysSearchRedirect row = redirectMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "重定向规则不存在");
        }
        return row;
    }

    private AdminSearchSynonymVO toSynonymVo(SysSearchSynonym row) {
        AdminSearchSynonymVO vo = new AdminSearchSynonymVO();
        vo.setId(row.getId());
        vo.setWord(row.getWord());
        vo.setSynonyms(row.getSynonyms());
        vo.setDomain(row.getDomain());
        vo.setCanonical(row.getCanonical());
        vo.setStatus(row.getStatus());
        if (row.getUpdateTime() != null) {
            vo.setUpdateTime(row.getUpdateTime().format(DT_FMT));
        }
        return vo;
    }

    private AdminSearchRedirectVO toRedirectVo(SysSearchRedirect row) {
        AdminSearchRedirectVO vo = new AdminSearchRedirectVO();
        vo.setId(row.getId());
        vo.setKeyword(row.getKeyword());
        vo.setTitle(row.getTitle());
        vo.setRoutePath(row.getRoutePath());
        vo.setNavTarget(HomeNavTargetValidator.parseLenient(row.getNavTarget()));
        vo.setPriority(row.getPriority());
        vo.setStatus(row.getStatus());
        vo.setRemark(row.getRemark());
        if (row.getUpdateTime() != null) {
            vo.setUpdateTime(row.getUpdateTime().format(DT_FMT));
        }
        return vo;
    }

    private AdminSearchIntentRuleVO toIntentRuleVo(SysSearchIntentRule row) {
        AdminSearchIntentRuleVO vo = new AdminSearchIntentRuleVO();
        vo.setId(row.getId());
        vo.setPattern(row.getPattern());
        vo.setIntentType(row.getIntentType());
        vo.setTargetKey(row.getTargetKey());
        vo.setTargetValue(row.getTargetValue());
        vo.setTargetPayload(row.getTargetPayload());
        vo.setPriority(row.getPriority());
        vo.setStatus(row.getStatus());
        if (row.getUpdateTime() != null) {
            vo.setUpdateTime(row.getUpdateTime().format(DT_FMT));
        }
        return vo;
    }

    private int normalizeStatus(Integer status) {
        return status != null && status == 0 ? 0 : 1;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private String requireText(String value, String label) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, label + "不能为空");
        }
        return value.trim();
    }
}
