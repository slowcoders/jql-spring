package org.slowcoders.hyperql.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.slowcoders.hyperql.jdbc.JdbcStorage;
import org.slowcoders.hyperql.jpa.JpaTable;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@MappedSuperclass
@DynamicInsert
public abstract class CyclicTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = READ_ONLY)
    @Getter
    Long id;

    @Schema(description = "Task 반복 실행 주기 (분 단위).\n 0인 경우, 매일 1회 수행.", example = "ex) 0")
    @Getter @Setter
    int intervalMinute;

    @Column(insertable = false)
    @Schema(accessMode = READ_ONLY)
    @Getter @Setter
    LocalDateTime lastExecutionTs;

    @JsonIgnore
    public Duration calcInterval() {
        return Duration.ofMinutes(intervalMinute);
    }

    @Schema(accessMode = READ_ONLY)
    public boolean isDailyTask() {
        return intervalMinute <= 0;
    }

    public static abstract class JQLRepository<TASK extends CyclicTask>
            extends JpaTable<TASK, Long> {

        protected JQLRepository(JdbcStorage storage, Class<TASK> entityType) {
            super(storage, entityType);
        }

        @Override
        public TASK insertEntity(TASK entity, InsertPolicy insertPolicy) {
            // validation 을 위해 호출.
            entity.calcInterval();
            return super.insertEntity(entity, insertPolicy);
        }
    }
}
