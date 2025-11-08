package apap.ti._5.tour_package_2306240156_be.restservice;

import apap.ti._5.tour_package_2306240156_be.model.Plan;
import apap.ti._5.tour_package_2306240156_be.restdto.request.CreatePlanRequestDTO;
import apap.ti._5.tour_package_2306240156_be.restdto.request.UpdatePlanRequestDTO;

import java.util.UUID;

public interface PlanRestService {
  Plan createPlan(String packageId, CreatePlanRequestDTO request);
  Plan getPlanById(UUID id);
  Plan updatePlan(UUID id, UpdatePlanRequestDTO request);
  void deletePlan(UUID id);
}

