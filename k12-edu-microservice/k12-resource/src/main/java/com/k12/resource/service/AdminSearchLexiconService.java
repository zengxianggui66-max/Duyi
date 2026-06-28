package com.k12.resource.service;

import com.k12.common.dto.AdminSearchIntentRuleVO;
import com.k12.common.dto.AdminSearchRedirectVO;
import com.k12.common.dto.AdminSearchRedirectWriteDTO;
import com.k12.common.dto.AdminSearchSynonymVO;
import com.k12.common.dto.AdminSearchSynonymWriteDTO;

import java.util.List;

public interface AdminSearchLexiconService {

    List<AdminSearchSynonymVO> listSynonyms(boolean includeDisabled, String domain);

    AdminSearchSynonymVO createSynonym(AdminSearchSynonymWriteDTO dto);

    AdminSearchSynonymVO updateSynonym(Long id, AdminSearchSynonymWriteDTO dto);

    void setSynonymStatus(Long id, Integer status);

    void deleteSynonym(Long id);

    AdminSearchSynonymVO createSynonymDraft(String keyword);

    List<AdminSearchRedirectVO> listRedirects(boolean includeDisabled);

    AdminSearchRedirectVO createRedirect(AdminSearchRedirectWriteDTO dto);

    AdminSearchRedirectVO updateRedirect(Long id, AdminSearchRedirectWriteDTO dto);

    void setRedirectStatus(Long id, Integer status);

    void deleteRedirect(Long id);

    List<AdminSearchIntentRuleVO> listIntentRules(boolean includeDisabled);
}
