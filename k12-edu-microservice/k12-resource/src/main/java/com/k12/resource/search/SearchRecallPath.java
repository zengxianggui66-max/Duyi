package com.k12.resource.search;

/** P2 多路召回来源 */
public enum SearchRecallPath {
    EXACT_TITLE,
    TOKEN_AND,
    TOKEN_OR,
    DIMENSION,
    SYNONYM,
    HOT_CLICK,
    CHANNEL,
    NEWS
}
