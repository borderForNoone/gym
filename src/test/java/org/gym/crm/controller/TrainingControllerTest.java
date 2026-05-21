package org.gym.crm.controller;

import org.gym.crm.dto.TrainingRequestDTO;
import org.gym.crm.facade.GymFacade;
import org.gym.crm.rest.TrainingTypeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {
    @Mock
    private GymFacade facade;
    @InjectMocks
    private TrainingController controller;

    @Test
    void addTraining_shouldReturnOkAndDelegateToFacade() {
        TrainingRequestDTO request = TrainingRequestDTO.builder().build();

        ResponseEntity<Void> response = controller.addTraining(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNull();
        verify(facade).createTraining(request);
    }

    @Test
    void getTrainingTypes_shouldReturnOkWithTypes() {
        List<TrainingTypeResponse> types = List.of(
                new TrainingTypeResponse(),
                new TrainingTypeResponse()
        );
        when(facade.getTrainingTypes()).thenReturn(types);

        ResponseEntity<List<TrainingTypeResponse>> response = controller.getTrainingTypes();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(types);
        verify(facade).getTrainingTypes();
    }

    @Test
    void getTrainingTypes_shouldReturnEmptyListWhenNoneExist() {
        when(facade.getTrainingTypes()).thenReturn(List.of());

        ResponseEntity<List<TrainingTypeResponse>> response = controller.getTrainingTypes();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEmpty();
        verify(facade).getTrainingTypes();
    }
}