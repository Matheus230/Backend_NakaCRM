package com.nakacorp.backend.repository;

import com.nakacorp.backend.model.LeadOrigem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadOrigemRepository extends JpaRepository<LeadOrigem, Long> {

    Optional<LeadOrigem> findByClienteId(Long clienteId);

    List<LeadOrigem> findByUtmSource(String utmSource);

    List<LeadOrigem> findByUtmCampaign(String utmCampaign);

    @Query("SELECT lo FROM LeadOrigem lo WHERE lo.utmSource = :source AND lo.utmCampaign = :campaign")
    List<LeadOrigem> findByUtmSourceAndUtmCampaign(@Param("source") String utmSource, @Param("campaign") String utmCampaign);

    @Query("SELECT DISTINCT lo.utmSource FROM LeadOrigem lo WHERE lo.utmSource IS NOT NULL ORDER BY lo.utmSource")
    List<String> findDistinctUtmSources();
}