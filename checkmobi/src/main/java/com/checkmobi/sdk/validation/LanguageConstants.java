package com.checkmobi.sdk.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LanguageConstants {
    public static final String[] IVR_SUPPORTED_LANGUAGES_VALUES = new String[] { "da-DK", "nl-NL", "en-AU", "en-GB", "en-US", "fr-FR", "fr-CA", "de-DE", "it-IT", "pl-PL", "pt-PT", "pt-BR", "ru-RU", "es-ES", "es-US", "sv-SE" };
    public static final Set<String> IVR_SUPPORTED_LANGUAGES_SET = new HashSet<>(Arrays.asList(IVR_SUPPORTED_LANGUAGES_VALUES));
}
