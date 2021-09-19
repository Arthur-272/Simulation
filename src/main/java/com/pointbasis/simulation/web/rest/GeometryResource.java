package com.pointbasis.simulation.web.rest;

import com.pointbasis.simulation.domain.Geometry;
import com.pointbasis.simulation.service.dto.GeometryDTO;
import com.pointbasis.simulation.repository.GeometryRepository;
import com.pointbasis.simulation.service.impl.GeometryServiceImpl;
import com.pointbasis.simulation.service.impl.JobServiceImpl;
import com.pointbasis.simulation.service.impl.MeshServiceImpl;
import com.pointbasis.simulation.web.rest.errors.BadRequestAlertException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.pointbasis.simulation.domain.Geometry}.
 */
@RestController
@RequestMapping("/api")
public class GeometryResource {

    private final Logger log = LoggerFactory.getLogger(GeometryResource.class);

    private static final String ENTITY_NAME = "geometry";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GeometryRepository geometryRepository;

    @Autowired
    private JobServiceImpl jobService;

    @Autowired
    private GeometryServiceImpl modelService;

    @Autowired
    private MeshServiceImpl meshService;

    public GeometryResource(GeometryRepository geometryRepository) {
        this.geometryRepository = geometryRepository;
    }

    @PostMapping("/project/{projectId}/model/{modelId}/simulate")
    public ResponseEntity simulateModel(@PathVariable String projectId, @PathVariable String modelId, @RequestBody GeometryDTO data)
        throws Exception {
        System.out.println("In Simulate Controller");
        return jobService.simulate(projectId, modelId, data);
    }

    @GetMapping("/models")
    public List<Geometry> getAllModelsByUser() throws Exception {
        return modelService.getAllModelsByUser();
    }

    @GetMapping("/project/{projectId}/models")
    public List<Geometry> getAllModelsByProject(@PathVariable String projectId) throws Exception {
        return modelService.getAllModelsByProject(projectId);
    }

    @GetMapping("/project/{projectId}/model/{modelId}/getModel")
    public ResponseEntity getFileContent(@PathVariable String projectId, @PathVariable String modelId, @RequestParam String format)
        throws Exception {
        return modelService.getFileContent(projectId, modelId, format);
    }

    /**
     * {@code POST  /geometries} : Create a new geometry.
     *
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new geometry, or with status {@code 400 (Bad Request)} if the geometry has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/project/{projectId}/models")
    public ResponseEntity<Geometry> createModel(@RequestBody MultipartFile object, @PathVariable String projectId) throws Exception {
        log.debug("REST request to save Model : {}", object);
        return modelService.createModel(object, projectId);
    }

    /**
     * {@code PUT  /geometries/:id} : Updates an existing geometry.
     *
     * @param id the id of the geometry to save.
     * @param geometry the geometry to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated geometry,
     * or with status {@code 400 (Bad Request)} if the geometry is not valid,
     * or with status {@code 500 (Internal Server Error)} if the geometry couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/geometries/{id}")
    public ResponseEntity<Geometry> updateGeometry(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody Geometry geometry
    ) throws URISyntaxException {
        log.debug("REST request to update Geometry : {}, {}", id, geometry);
        if (geometry.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, geometry.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!geometryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Geometry result = geometryRepository.save(geometry);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, geometry.getId()))
            .body(result);
    }

    /**
     * {@code PATCH  /geometries/:id} : Partial updates given fields of an existing geometry, field will ignore if it is null
     *
     * @param id the id of the geometry to save.
     * @param geometry the geometry to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated geometry,
     * or with status {@code 400 (Bad Request)} if the geometry is not valid,
     * or with status {@code 404 (Not Found)} if the geometry is not found,
     * or with status {@code 500 (Internal Server Error)} if the geometry couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/geometries/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<Geometry> partialUpdateGeometry(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody Geometry geometry
    ) throws URISyntaxException {
        log.debug("REST request to partial update Geometry partially : {}, {}", id, geometry);
        if (geometry.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, geometry.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!geometryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Geometry> result = geometryRepository
            .findById(geometry.getId())
            .map(
                existingGeometry -> {
                    if (geometry.getName() != null) {
                        existingGeometry.setName(geometry.getName());
                    }

                    return existingGeometry;
                }
            )
            .map(geometryRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, geometry.getId())
        );
    }

    /**
     * {@code GET  /geometries} : get all the geometries.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of geometries in body.
     */
    @GetMapping("/geometries")
    public List<Geometry> getAllGeometries() {
        log.debug("REST request to get all Geometries");
        return geometryRepository.findAll();
    }

    /**
     * {@code GET  /geometries/:id} : get the "id" geometry.
     *
     * @param id the id of the geometry to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the geometry, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/geometries/{id}")
    public ResponseEntity<Geometry> getGeometry(@PathVariable String id) {
        log.debug("REST request to get Geometry : {}", id);
        Optional<Geometry> geometry = geometryRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(geometry);
    }

    /**
     * {@code DELETE  /geometries/:id} : delete the "id" geometry.
     *
     * @param id the id of the geometry to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/models/{id}")
    public ResponseEntity<Void> deleteGeometry(@PathVariable String id) throws Exception {
        log.debug("REST request to delete Geometry : {}", id);
        modelService.deleteModel(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }
}
