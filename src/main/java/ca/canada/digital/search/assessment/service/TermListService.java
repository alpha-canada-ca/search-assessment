package ca.canada.digital.search.assessment.service;

import ca.canada.digital.search.assessment.api.CreateTermRequest;
import ca.canada.digital.search.assessment.api.UpsertTermRequest;
import ca.canada.digital.search.assessment.dao.*;
import ca.canada.digital.search.assessment.model.*;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TermListService {
    private final TermListDao termListDao;
    private final TermDao termDao;
    private final LanguageDao languageDao;
    private final UserEntityDao userDao;
    private final TargetUrlDao targetUrlDao;

    public TermListService(TermListDao termListDao,
                           TermDao termDao,
                           LanguageDao languageDao,
                           UserEntityDao userDao,
                           TargetUrlDao targetUrlDao) {
        this.termListDao = termListDao;
        this.termDao = termDao;
        this.languageDao = languageDao;
        this.userDao = userDao;
        this.targetUrlDao = targetUrlDao;
    }

    public TermList getTermList(Integer listId) {
        return termListDao
                .findById(listId)
                .orElseThrow(() -> new BadRequestException("Invalid list"));
    }

    /**
     * Create a new TermList for the authenticated user.
     */
    public TermList createTermList(UserEntity requester,
                                   String name,
                                   Integer languageId) {

        UserEntity user = userDao.findById(requester.getId())
                .orElseThrow(() -> new NotFoundException("User not found: " + requester.getId()));

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
    public Term addTerm(UserEntity requester,
                        Integer listId,
                        String termText,
                        Integer index,
                        List<String> targetUrls) {

        UserEntity user = userDao.findById(requester.getId())
                .orElseThrow(() -> new NotFoundException("User not found: " + requester.getId()));

        TermList list = termListDao.findById(listId)
                .orElseThrow(() -> new NotFoundException("TermList not found: " + listId));

        // only list owner or admin may add
        if (!user.isAdmin() && !list.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not allowed to add terms to this list");
        }

        Term term = new Term();
        term.setTerm(termText);
        term.setSequence(index);
        term.setLastUpdate(LocalDateTime.now());
        term.setTermList(list);
        term.setUser(user);
        term.setLanguage(list.getLanguage());

        if (targetUrls != null) {
            for (String targetUrl : targetUrls) {
                TargetUrl t = new TargetUrl();
                t.setUrl(targetUrl);
                t.setTerm(term);
                term.getTargetUrls().add(t);
            }
        }

        return termDao.save(term);
    }

    /**
     * Add multiple Terms to a TermList in bulk.
     */
    public List<Term> addTermsBulk(UserEntity requester,
                                   Integer listId,
                                   List<CreateTermRequest> requests) {

        UserEntity user = userDao.findById(requester.getId())
                .orElseThrow(() -> new NotFoundException("User not found: " + requester.getId()));

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
            term.setSequence(req.getPosition());
            term.setLastUpdate(now);
            term.setTermList(list);
            term.setUser(user);
            term.setLanguage(list.getLanguage());

            if (req.getTargetUrls() != null) {
                for (String targetUrl : req.getTargetUrls()) {
                    TargetUrl t = new TargetUrl();
                    t.setUrl(targetUrl);
                    t.setTerm(term);
                    term.getTargetUrls().add(t);
                }
            }

            results.add(termDao.save(term));
        }
        return results;
    }

    public List<Term> upsertTerms(
            UserEntity user,
            Integer listId,
            List<UpsertTermRequest> reqs
    ) {
        List<Term> result = new ArrayList<>();
        for (UpsertTermRequest r : reqs) {
            if (r.getId() != null) {
                result.add(updateTerm(
                        user,
                        listId,
                        r.getId(),
                        r.getTerm(),
                        r.getPosition(),
                        r.getTargetUrls()
                ));
            } else {
                result.add(addTerm(
                        user,
                        listId,
                        r.getTerm(),
                        r.getPosition(), r.getTargetUrls().stream().map(TargetUrl::getUrl).collect(Collectors.toList())
                ));
            }
        }
        return result;
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

    public List<TermList> listByMe(UserEntity requester) {
        return termListDao.findByUser(requester.getId());
    }

    /**
     * Only admins or owner or same-department can view terms in a list
     */
    public List<Term> listTerms(UserEntity requester,
                                Integer listId) {
        UserEntity user = userDao.findById(requester.getId())
                .orElseThrow(() -> new NotFoundException("User not found: " + requester.getId()));


        TermList list = termListDao.findById(listId)
                .orElseThrow(() -> new NotFoundException("TermList not found: " + listId));
        boolean owner = list.getUser().getId().equals(user.getId());
        boolean sameDept = list.getDepartment().getId()
                .equals(user.getDepartment().getId());
        if (!(user.isAdmin() || owner || sameDept)) {
            throw new ForbiddenException("Not allowed to view terms for list " + listId);
        }
        return termDao.findByTermList(listId);
    }

    public void deleteList(UserEntity requester, Integer listId) {
        UserEntity user = userDao.findById(requester.getId())
                .orElseThrow(() -> new NotFoundException("User not found: " + requester.getId()));

        TermList list = termListDao.findById(listId)
                .orElseThrow(() -> new NotFoundException("List not found: " + listId));
        // Only admins and list owners can delete lists
        if (!user.isAdmin() && !list.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Only admins and owners may delete lists");
        }
        termListDao.delete(list);
    }

    public Term updateTerm(
            UserEntity requester,
            Integer listId,
            Integer termId,
            String newTerm,
            Integer newPosition,
            List<TargetUrl> newTargetUrls
    ) {

        UserEntity user = userDao.findById(requester.getId())
                .orElseThrow(() -> new NotFoundException("User not found: " + requester.getId()));

        TermList list = termListDao.findById(listId)
                .orElseThrow(() -> new NotFoundException("TermList not found: " + listId));

        // only list owner or admin may add
        if (!user.isAdmin() && !list.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Not allowed to add terms to this list");
        }

        Optional<Term> term = termDao.findById(termId);
        if (term.isEmpty() || !term.get().getTermList().getId().equals(listId)) {
            throw new WebApplicationException("Not found", 404);
        }
        term.get().setTerm(newTerm);
        term.get().setSequence(newPosition);

        // Build a map of existing URLs by id
        Map<Integer, TargetUrl> existing = term.get().getTargetUrls().stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(TargetUrl::getId, u -> u));

        // New collection of URLs for the term
        List<TargetUrl> newCollection = new ArrayList<>();

        for (TargetUrl url : newTargetUrls) {
            if (url.getId() != null && existing.containsKey(url.getId())) {
                // 1) Update existing targetUrl
                TargetUrl managed = existing.get(url.getId());
                managed.setUrl(url.getUrl());
                newCollection.add(managed);
                existing.remove(url.getId());
            } else {
                // 2) Insert brand‑new
                TargetUrl toPersist = new TargetUrl();
                toPersist.setUrl(url.getUrl());
                toPersist.setTerm(term.get());
                targetUrlDao.save(toPersist);
                newCollection.add(toPersist);
            }
        }

        // 3) Any remaining in `existing` are URLs the user removed, so delete them
        for (TargetUrl removed : existing.values()) {
            term.get().getTargetUrls().remove(removed);
            targetUrlDao.delete(removed);
        }

        // 4) clear the same persistent collection and add the new ones
        List<TargetUrl> persistentTargets = term.get().getTargetUrls();
        persistentTargets.clear();
        persistentTargets.addAll(newCollection);

        return termDao.save(term.get());
    }


}
