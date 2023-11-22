package com.io.codesystem.search.icd;

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
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IcdCodeSearchService {

	@Autowired
	public IcdCodeSearchRepository icdCodeSearchRepository;

	@Autowired
	private EntityManager entityManager;

//	public IcdCodeSearchService(final EntityManagerFactory entityManagerFactory) {
//		this.entityManager = entityManagerFactory.createEntityManager();
//	}
	
	@Transactional
	public void createIcdIndex() throws Exception {

		SearchSession searchSession = Search.session(entityManager);
		MassIndexer massIndexer = searchSession.massIndexer(IcdCode.class);
		// massIndexer.type(IcdCode.class).reindexOnly("e.type='v'");
		massIndexer.type(IcdCode.class);
		massIndexer.idFetchSize(250).batchSizeToLoadObjects(200).threadsToLoadObjects(4).startAndWait();
	}

	public Page<IcdCode> searchIcdCode(String searchTerm, Pageable pageable) {

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
		SearchResult<IcdCode> result = searchSession.search(IcdCode.class)
//				.where(f -> f.match().fields("icd10code","shortDesc", "longDesc")
//						.matching(searchTerm)
//						//.fuzzy(maxEditDistance)//To cover spelling corrections
//						).fetch(pageable.getPageSize());
				.where(f -> f.bool().should(f.match().field("icd10code").matching(searchTermUrlEncoded)
				//
				).should(f.match().fields("shortDesc", "longDesc").matching(searchTermUrlEncoded).fuzzy(maxEditDistance,
						minPrefixLength) // To cover spelling corrections

				)).fetch(Integer.MAX_VALUE);

		long totalHitCount = result.total().hitCount();
		List<IcdCode> allHits = result.hits();
		System.out.println("Total Hits Count:" + totalHitCount);
		System.out.println(" Hits Count:" + allHits.size());
		int fromIndex = pageNumber * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, allHits.size());
		List<IcdCode> hits = allHits.subList(fromIndex, toIndex);
		// List<IcdCode> hits = result.hits();
		return new PageImpl<>(hits, pageable, totalHitCount);
	}

//public List<IcdCodeTree> get_Icd_tree(Integer icd10id)
//{
//	return icdCodeSearchRepository.get_Icd_tree(icd10id);
//}
//public Page<IcdCodeTree> get_Icd_tree(Integer icd10id,Pageable pageable)
//{
//	Page<IcdCodeTree> icdList =icdCodeSearchRepository.get_Icd_tree(icd10id,pageable);
//	 int startIndex = pageable.getPageNumber() * pageable.getPageSize();
//	    int endIndex = Math.min(startIndex + pageable.getPageSize(), icdList.size());
//	    List<IcdCodeTree> pageOfItems = icdList.subList(startIndex, endIndex);
//	   
//	    return new PageImpl<>(pageOfItems,pageable, icdList.size());
//}
	public Page<IcdCodeTree> get_Icd_tree(Integer icd10id, Pageable pageable) {
		List<IcdCodeTree> icdList = icdCodeSearchRepository.get_Icd_tree(icd10id);
		int startIndex = pageable.getPageNumber() * pageable.getPageSize();
		int endIndex = Math.min(startIndex + pageable.getPageSize(), icdList.size());
		List<IcdCodeTree> pageOfItems = icdList.subList(startIndex, endIndex);

		return new PageImpl<>(pageOfItems, pageable, icdList.size());
	}
//public List<IcdCodeTree> getICDParentAndSiblings(Integer parentId)
//{
//	return icdCodeSearchRepository.getICDParentAndSiblings(parentId);
//}
}