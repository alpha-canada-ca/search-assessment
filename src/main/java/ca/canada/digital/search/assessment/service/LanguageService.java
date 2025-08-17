package ca.canada.digital.search.assessment.service;

import ca.canada.digital.search.assessment.dao.LanguageDao;
import ca.canada.digital.search.assessment.dao.UserEntityDao;
import ca.canada.digital.search.assessment.dao.UserSessionDao;
import ca.canada.digital.search.assessment.model.Language;
import ca.canada.digital.search.assessment.model.UserEntity;
import ca.canada.digital.search.assessment.model.UserSession;
import jakarta.ws.rs.NotAuthorizedException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class LanguageService {
    private final LanguageDao languageDao;

    public LanguageService(LanguageDao languageDao) {
        this.languageDao = languageDao;
    }

    public List<Language> listLanguages() {
        return languageDao.findAll();
    }

    public Optional<Language> getLanguage(Integer id) {
        return languageDao.findById(id);
    }

}
