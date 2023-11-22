package com.io.codesystem.search.medicine;

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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MedicineService {

	@Autowired
	MedicineRepository medicinerepository;

	@Autowired
	private EntityManager entityManager;

	@Transactional
	public void createMedicineIndex() throws Exception {

		SearchSession searchSession = Search.session(entityManager);
		MassIndexer massIndexer = searchSession.massIndexer(Medicine.class);
		massIndexer.type(Medicine.class);
		// massIndexer.type(Medicine.class).reindexOnly("e.versionState='Valid'");
		// reindexOnly("e.type='v'");

		massIndexer.idFetchSize(250).batchSizeToLoadObjects(200).threadsToLoadObjects(4).startAndWait();
	}

	public Page<Medicine> searchMedicine(String searchTerm, Pageable pageable) {
		// TODO Auto-generated method stub
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		int maxEditDistance = 2;
		int minPrefixLength = 3;

		SearchSession searchSession = Search.session(entityManager);
		SearchResult<Medicine> result = searchSession.search(Medicine.class)
				.where(f -> f.bool().should(f.match().field("ndc").matching(searchTerm))
						.should(f.match().fields("name").matching(searchTerm).fuzzy(maxEditDistance, minPrefixLength)))
				.fetch(Integer.MAX_VALUE);

		long totalHitCount = result.total().hitCount();
		List<Medicine> allHits = result.hits();
		log.info("===== Total Hits Count :: " + totalHitCount);
		log.info("===== Hits Count :: " + allHits.size());
		int fromIndex = pageNumber * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, allHits.size());
		List<Medicine> hits = allHits.subList(fromIndex, toIndex);

		return new PageImpl<>(hits, pageable, totalHitCount);
	}

}
