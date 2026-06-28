package com.k12.resource.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * P2 意图识别单元测试（无 DB）
 */
class SearchIntentRecognizerTest {

    @Test
    void recognizePrimaryChineseGrade1Courseware() {
        SearchLexiconService lexicon = mock(SearchLexiconService.class);
        when(lexicon.allAliasesByLengthDesc()).thenReturn(java.util.List.of(
                "小学", "一年级", "语文", "课件", "初一", "七年级", "小一"));
        when(lexicon.isStopWord(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(lexicon.canonicalOf("小一")).thenReturn("一年级");
        when(lexicon.canonicalOf("初一")).thenReturn("七年级");
        when(lexicon.canonicalOf(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(inv -> inv.getArgument(0, String.class));

        SearchChineseNormalizer normalizer = new SearchChineseNormalizer(lexicon);
        SearchQueryParser parser = new SearchQueryParser(lexicon);
        SearchIntentRecognizer recognizer = new SearchIntentRecognizer(
                normalizer, parser, mock(com.k12.resource.mapper.SysSearchIntentRuleMapper.class));

        ParsedSearchQuery parsed = recognizer.recognize("小学语文一年级课件");
        assertEquals("primary", parsed.getStageKey());
        assertEquals("语文", parsed.getSubject());
        assertEquals("chinese", parsed.getSubjectKey());
        assertEquals("一年级", parsed.getGradeName());
        assertEquals("课件", parsed.getTeachingType());
        assertEquals("courseware", parsed.getResourceTypeKey());
        assertEquals("同步备课", parsed.getModuleName());

        var vo = recognizer.toIntentVo(parsed);
        assertNotNull(vo);
        assertEquals("primary", vo.getStage());
        assertEquals("chinese", vo.getSubject());
        assertEquals("一年级", vo.getGrade());
        assertEquals("courseware", vo.getResourceType());
        assertEquals("同步备课", vo.getModule());
    }
}
