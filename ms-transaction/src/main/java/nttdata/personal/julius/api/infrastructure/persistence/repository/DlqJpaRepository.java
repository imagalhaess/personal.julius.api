package nttdata.personal.julius.api.infrastructure.persistence.repository;

import nttdata.personal.julius.api.infrastructure.persistence.entity.DlqEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DlqJpaRepository extends JpaRepository<DlqEntity, Long> {

    List<DlqEntity> findByTransactionId(String transactionId);

    List<DlqEntity> findBySourceService(String sourceService);
}
