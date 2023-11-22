package com.io.codesystem.search.cpt;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CptCodeSearchService {

	@Autowired
	CptCodeCategorySearchRepository cptCodeCategorySearchRepository;

	@Autowired
	private EntityManager entityManager;

	@Transactional
	public void createCptIndex() throws Exception {

		SearchSession searchSession = Search.session(entityManager);
		MassIndexer massIndexer = searchSession.massIndexer(CptCode.class);
		massIndexer.type(CptCode.class);
		// massIndexer.type(CptCode.class).reindexOnly("e.versionState='Valid'");

		massIndexer.idFetchSize(250).batchSizeToLoadObjects(200).threadsToLoadObjects(4).startAndWait();
	}

	public Page<CptCode> searchCptCode(String searchTerm, Pageable pageable) {
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		int maxEditDistance = 2;
		int minPrefixLength = 3;
		SearchSession searchSession = Search.session(entityManager);
		String searchTermUrlEncoded;
		try {
			searchTermUrlEncoded = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}
		SearchResult<CptCode> result = searchSession.search(CptCode.class)

				.where(f -> f.bool().should(f.match().field("code").matching(searchTermUrlEncoded)
				//
				).should(f.match().fields("shortDesc").matching(searchTermUrlEncoded).fuzzy(maxEditDistance,
						minPrefixLength) // To cover spelling corrections

				)).fetch(Integer.MAX_VALUE);

		long totalHitCount = result.total().hitCount();
		List<CptCode> allHits = result.hits();
		int fromIndex = pageNumber * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, allHits.size());
		List<CptCode> hits = allHits.subList(fromIndex, toIndex);

		return new PageImpl<>(hits, pageable, totalHitCount);
	}

	/*
	 * public List<CptCodeCategory> getCptCodeByCodeOrShortWithCtg(String
	 * codeorshort) { // TODO Auto-generated method stub return
	 * cptCodeCategorySearchRepository.getCptCodeByCodeOrShortWithCtg(codeorshort);
	 * }
	 */

}
