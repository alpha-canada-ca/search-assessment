package ca.canada.digital.search.assessment.service;

import ca.canada.digital.search.assessment.api.CreateTermRequest;
import ca.canada.digital.search.assessment.dao.LanguageDao;
import ca.canada.digital.search.assessment.dao.TermDao;
import ca.canada.digital.search.assessment.dao.TermListDao;
import ca.canada.digital.search.assessment.model.*;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TermListService {
    private final TermListDao termListDao;
    private final TermDao termDao;
    private final LanguageDao languageDao;

    public TermListService(TermListDao termListDao,
                           TermDao termDao,
                           LanguageDao languageDao) {
        this.termListDao = termListDao;
        this.termDao = termDao;
        this.languageDao = languageDao;
    }

    /**
     * Create a new TermList for the authenticated user.
     */
    public TermList createTermList(UserEntity user,
                                   String name,
                                   Integer languageId) {
        Language lang = languageDao.findById(languageId)
                .orElseThrow(() -> new NotFoundException("Language not found: " + languageId));

        TermList list = new TermList();
        list.setName(name);
        list.setUser(user);
        list.setDepartment(user.getDepartment());
        list.setLanguage(lang);

        return termListDao.save(list);
    }

    /**
     * Add a Term to an existing TermList, with optional target URLs.
     */
    public Term addTerm(UserEntity user,
                        Integer listId,
                        String termText,
                        Integer index,
                        List<String> targetUrls) {
        TermList list = termListDao.findById(listId)
                .orElseThrow(() -> new NotFoundException("TermList not found: " + listId));

        // only list owner or admin may add
        if (!user.isAdmin() && !list.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not allowed to add terms to this list");
        }

        Term term = new Term();
        term.setTerm(termText);
        term.setIndex(index);
        term.setLastUpdate(LocalDateTime.now());
        term.setTermList(list);
        term.setUser(user);
        term.setLanguage(list.getLanguage());

        if (targetUrls != null) {
            for (String url : targetUrls) {
                TargetUrl t = new TargetUrl();
                t.setUrl(url);
                t.setTerm(term);
                term.getTargets().add(t);
            }
        }

        return termDao.save(term);
    }

    /**
     * Add multiple Terms to a TermList in bulk.
     */
    public List<Term> addTermsBulk(UserEntity user,
                                   Integer listId,
                                   List<CreateTermRequest> requests) {
        TermList list = termListDao.findById(listId)
                .orElseThrow(() -> new NotFoundException("TermList not found: " + listId));

        if (!user.isAdmin() && !list.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not allowed to add terms to this list");
        }

        List<Term> results = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (CreateTermRequest req : requests) {
            Term term = new Term();
            term.setTerm(req.getTerm());
            term.setIndex(req.getIndex());
            term.setLastUpdate(now);
            term.setTermList(list);
            term.setUser(user);
            term.setLanguage(list.getLanguage());

            if (req.getTargetUrls() != null) {
                for (String url : req.getTargetUrls()) {
                    TargetUrl t = new TargetUrl();
                    t.setUrl(url);
                    t.setTerm(term);
                    term.getTargets().add(t);
                }
            }

            results.add(termDao.save(term));
        }
        return results;
    }

    /**
     * Only admins or same-department can view lists for a department
     */
    public List<TermList> listByDepartment(UserEntity requester,
                                           Integer departmentId) {
        if (!requester.isAdmin()
                && !requester.getDepartment().getId().equals(departmentId)) {
            throw new ForbiddenException("Not allowed to view lists for department " + departmentId);
        }
        return termListDao.findByDepartment(departmentId);
    }

    /**
     * Only admins or the user themselves or same-department may view another user’s lists
     */
    public List<TermList> listByUser(UserEntity requester,
                                     Integer userId) {
        boolean sameUser = requester.getId().equals(userId);
        boolean sameDept = requester.getDepartment().getId()
                .equals(termListDao.findByUser(userId).stream()
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("No lists for user " + userId))
                        .getDepartment().getId());
        if (!(requester.isAdmin() || sameUser || sameDept)) {
            throw new ForbiddenException("Not allowed to view lists for user " + userId);
        }
        return termListDao.findByUser(userId);
    }

    /**
     * Only admins or owner or same-department can view terms in a list
     */
    public List<Term> listTerms(UserEntity requester,
                                Integer listId) {
        TermList list = termListDao.findById(listId)
                .orElseThrow(() -> new NotFoundException("TermList not found: " + listId));
        boolean owner = list.getUser().getId().equals(requester.getId());
        boolean sameDept = list.getDepartment().getId()
                .equals(requester.getDepartment().getId());
        if (!(requester.isAdmin() || owner || sameDept)) {
            throw new ForbiddenException("Not allowed to view terms for list " + listId);
        }
        return termDao.findByTermList(listId);
    }

}
