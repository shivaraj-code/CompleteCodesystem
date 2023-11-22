package com.io.codesystem.search.pharmacy;

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
public class PharmacySearchService {

	@Autowired
	PharmaciesRepository pharmRep;

	@Autowired
	private EntityManager entityManager;

	int maxEditDistance = 2;
	int minPrefixLength = 3;

	public String helle() {
		return "hella there! gudaI";

	}

	public List<Pharmacies> getAllPharmacies() {
		return pharmRep.findAll();

	}

	public Page<Pharmacies> searchPharmacy(String searchTerm, Pageable pageable) {

		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();

		SearchSession searchSession = Search.session(entityManager);
		SearchResult<Pharmacies> result = searchSession.search(Pharmacies.class)
				.where(f -> f.bool().should(f.match().field("zip").matching(searchTerm)

				).should(f.match().fields("name", "address1").matching(searchTerm).fuzzy(maxEditDistance,
						minPrefixLength) // To cover spelling corrections

				)).fetch(Integer.MAX_VALUE);

		long totalHitCount = result.total().hitCount();
//		System.out.println("Total Hits Count:" + totalHitCount);
//		System.out.println(" Hits Count:" + result.hits().size());
		List<Pharmacies> allHits = result.hits();
		int fromIndex = pageNumber * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, allHits.size());
		List<Pharmacies> hits = allHits.subList(fromIndex, toIndex);
		return new PageImpl<>(hits, pageable, totalHitCount);

	}

	@Transactional
	public void createPharmacyIndex() throws Exception {
		SearchSession searchSession = Search.session(entityManager);
		MassIndexer massIndexer = searchSession.massIndexer(Pharmacies.class);
		massIndexer.type(Pharmacies.class);

		massIndexer.idFetchSize(250).batchSizeToLoadObjects(200).threadsToLoadObjects(4).startAndWait();

	}

	public Page<Pharmacies> searchPharmacyZip(String searchTerm, Pageable pageable) {
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		SearchSession searchSession = Search.session(entityManager);
		SearchResult<Pharmacies> result = searchSession.search(Pharmacies.class)
				.where(f -> f.bool().should(f.match().field("zip").matching(searchTerm))).fetch(Integer.MAX_VALUE);
		long totalHitCount = result.total().hitCount();
		List<Pharmacies> allHits = result.hits();
		int fromIndex = pageNumber * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, allHits.size());
		List<Pharmacies> hits = allHits.subList(fromIndex, toIndex);
		return new PageImpl<>(hits, pageable, totalHitCount);
	}

	public Page<Pharmacies> searchPharmacyName(String searchTerm, Pageable pageable) {
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		SearchSession searchSession = Search.session(entityManager);
		SearchResult<Pharmacies> result = searchSession.search(Pharmacies.class)
				.where(f -> f.bool()
						.should(f.match().fields("name").matching(searchTerm).fuzzy(maxEditDistance, minPrefixLength)))
				.fetch(Integer.MAX_VALUE);
		long totalHitCount = result.total().hitCount();
		List<Pharmacies> allHits = result.hits();
		int fromIndex = pageNumber * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, allHits.size());
		List<Pharmacies> hits = allHits.subList(fromIndex, toIndex);
		return new PageImpl<>(hits, pageable, totalHitCount);
	}

	public Page<Pharmacies> searchPharmacyAddress(String searchTerm, Pageable pageable) {
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		SearchSession searchSession = Search.session(entityManager);
		SearchResult<Pharmacies> result = searchSession.search(Pharmacies.class)
				.where(f -> f.bool().should(
						f.match().fields("address1").matching(searchTerm).fuzzy(maxEditDistance, minPrefixLength)))
				.fetch(Integer.MAX_VALUE);
		long totalHitCount = result.total().hitCount();
		List<Pharmacies> allHits = result.hits();
		int fromIndex = pageNumber * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, allHits.size());
		List<Pharmacies> hits = allHits.subList(fromIndex, toIndex);
		return new PageImpl<>(hits, pageable, totalHitCount);
	}

	public Page<Pharmacies> searchPharmacyNcpdpid(String searchTerm, Pageable pageable) {
		// TODO Auto-generated method stub
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		SearchSession searchSession = Search.session(entityManager);
		SearchResult<Pharmacies> result = searchSession.search(Pharmacies.class)
				.where(f -> f.bool().should(
						f.match().fields("ncpdpid").matching(searchTerm).fuzzy(maxEditDistance, minPrefixLength)))
				.fetch(Integer.MAX_VALUE);
		long totalHitCount = result.total().hitCount();
		List<Pharmacies> allHits = result.hits();
		int fromIndex = pageNumber * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, allHits.size());
		List<Pharmacies> hits = allHits.subList(fromIndex, toIndex);
		return new PageImpl<>(hits, pageable, totalHitCount);
	}

}
