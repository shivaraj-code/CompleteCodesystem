package com.io.codesystem.search.allergies;

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
public class Allergieservice {

	@Autowired
	public AllergiesRepository allergiesRepository;

	@Autowired
	private EntityManager entityManager;

	@Transactional
	public void createAllergiesIndex() throws Exception {

		SearchSession searchSession = Search.session(entityManager);
		MassIndexer massIndexer = searchSession.massIndexer(Allergies.class);
		massIndexer.type(Allergies.class);
		// .reindexOnly("e.type='v'");

		massIndexer.idFetchSize(250).batchSizeToLoadObjects(200).threadsToLoadObjects(4).startAndWait();
	}

	public Page<Allergies> searchAllergiesCode(String searchTerm, Pageable pageable) {

		int maxEditDistance = 2;
		int minPrefixLength = 3;
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();

		SearchSession searchSession = Search.session(entityManager);
		SearchResult<Allergies> result = searchSession.search(Allergies.class)

				.where(f -> f.bool()
						.should(f.match().fields("damConceptIdDesc", "damAlrgnGrpDesc", "allergyDesc")
								.matching(searchTerm).fuzzy(maxEditDistance, minPrefixLength))) // To cover spelling
																		 						// corrections
				// .fetch(pageable.getPageSize());
				.fetch(Integer.MAX_VALUE);

//		            .should(f.match()
//		                .fields("shortName")
//		                .matching(searchTerm)
//		                .fuzzy(maxEditDistance, minPrefixLength)))
//		        .fetch(Integer.MAX_VALUE);

//				.where(f -> f.bool()
//						.should(f.wildcard().field("damConceptIdDesc").matching(searchTerm + "*"))
//						.should(f.wildcard().field("damAlrgnGrpDesc").matching(searchTerm + "*"))
//					.should(f.wildcard().field("allergyDesc").matching(searchTerm + "*"))
//					.should(f.simpleQueryString().fields("damConceptIdDesc", "damAlrgnGrpDesc", "allergyDesc")
//				    .matching(searchTerm)))
//			        .fetch(pageable.getPageSize());

		// .f.where(f -> f.wildcard().fields("damConceptIdDesc",
		// "damAlrgnGrpDesc","allergyDesc").matching(searchTerm)).fetch(pageable.getPageSize());

		long totalHitCount = result.total().hitCount();
		List<Allergies> allHits = result.hits();
		int fromIndex = pageNumber * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, allHits.size());
		List<Allergies> hits = allHits.subList(fromIndex, toIndex);
		System.out.println("Total Hits Count:" + totalHitCount);
		System.out.println(" Hits Count:" + result.hits().size());
		return new PageImpl<>(hits, pageable, totalHitCount);

	}

}
