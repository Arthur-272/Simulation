package com.pointbasis.simulation.web.rest;

import com.pointbasis.simulation.domain.Mesh;
import com.pointbasis.simulation.repository.MeshRepository;
import com.pointbasis.simulation.service.Services;
import com.pointbasis.simulation.service.impl.MeshServiceImpl;
import com.pointbasis.simulation.web.rest.errors.BadRequestAlertException;
import java.net.URI;
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
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.pointbasis.simulation.domain.Mesh}.
 */
@RestController
@RequestMapping("/api")
public class MeshResource {

    private final Logger log = LoggerFactory.getLogger(MeshResource.class);

    private static final String ENTITY_NAME = "mesh";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MeshRepository meshRepository;

    @Autowired
    private MeshServiceImpl meshService;

    @Autowired
    private Services services;

    public MeshResource(MeshRepository meshRepository) {
        this.meshRepository = meshRepository;
    }

    @GetMapping("/project/{projectId}/meshes")
    public List<Mesh> getAllModelsByProject(@PathVariable String projectId) throws Exception {
        return meshService.getAllMeshesByProject(projectId);
    }

    @GetMapping("/project/{projectId}/mesh/{meshId}/getMesh")
    public ResponseEntity getFileContent(@PathVariable String projectId, @PathVariable String meshId, @RequestParam String format)
        throws Exception {
        return meshService.getFileContent(projectId, meshId, format);
    }

    @GetMapping("/project/{projectId}/mesh/{meshId}/download")
    public String downloadMesh(@PathVariable String projectId, @PathVariable String meshId, @RequestParam String format) throws Exception {
        return meshService.downloadObject(projectId, meshId, format);
    }

    /**
     * {@code POST  /meshes} : Create a new mesh.
     *
     * @param mesh the mesh to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new mesh, or with status {@code 400 (Bad Request)} if the mesh has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/meshes")
    public ResponseEntity<Mesh> createMesh(@RequestBody Mesh mesh) throws URISyntaxException {
        log.debug("REST request to save Mesh : {}", mesh);
        if (mesh.getId() != null) {
            throw new BadRequestAlertException("A new mesh cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Mesh result = meshRepository.save(mesh);
        return ResponseEntity
            .created(new URI("/api/meshes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId()))
            .body(result);
    }

    /**
     * {@code PUT  /meshes/:id} : Updates an existing mesh.
     *
     * @param id the id of the mesh to save.
     * @param mesh the mesh to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated mesh,
     * or with status {@code 400 (Bad Request)} if the mesh is not valid,
     * or with status {@code 500 (Internal Server Error)} if the mesh couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/meshes/{id}")
    public ResponseEntity<Mesh> updateMesh(@PathVariable(value = "id", required = false) final String id, @RequestBody Mesh mesh)
        throws URISyntaxException {
        log.debug("REST request to update Mesh : {}, {}", id, mesh);
        if (mesh.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, mesh.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!meshRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Mesh result = meshRepository.save(mesh);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, mesh.getId()))
            .body(result);
    }

    /**
     * {@code PATCH  /meshes/:id} : Partial updates given fields of an existing mesh, field will ignore if it is null
     *
     * @param id the id of the mesh to save.
     * @param mesh the mesh to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated mesh,
     * or with status {@code 400 (Bad Request)} if the mesh is not valid,
     * or with status {@code 404 (Not Found)} if the mesh is not found,
     * or with status {@code 500 (Internal Server Error)} if the mesh couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/meshes/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<Mesh> partialUpdateMesh(@PathVariable(value = "id", required = false) final String id, @RequestBody Mesh mesh)
        throws URISyntaxException {
        log.debug("REST request to partial update Mesh partially : {}, {}", id, mesh);
        if (mesh.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, mesh.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!meshRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Mesh> result = meshRepository
            .findById(mesh.getId())
            .map(
                existingMesh -> {
                    if (mesh.getEdgeLength() != null) {
                        existingMesh.setEdgeLength(mesh.getEdgeLength());
                    }
                    if (mesh.getTolerance() != null) {
                        existingMesh.setTolerance(mesh.getTolerance());
                    }

                    return existingMesh;
                }
            )
            .map(meshRepository::save);

        return ResponseUtil.wrapOrNotFound(result, HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, mesh.getId()));
    }

    /**
     * {@code GET  /meshes} : get all the meshes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of meshes in body.
     */
    @GetMapping("/meshes")
    public List<Mesh> getAllMeshes() {
        log.debug("REST request to get all Meshes");
        return meshRepository.findAll();
    }

    /**
     * {@code GET  /meshes/:id} : get the "id" mesh.
     *
     * @param id the id of the mesh to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the mesh, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/meshes/{id}")
    public ResponseEntity<Mesh> getMesh(@PathVariable String id) {
        log.debug("REST request to get Mesh : {}", id);
        Optional<Mesh> mesh = meshRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(mesh);
    }

    /**
     * {@code DELETE  /meshes/:id} : delete the "id" mesh.
     *
     * @param id the id of the mesh to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/meshes/{id}")
    public ResponseEntity<Void> deleteMesh(@PathVariable String id) throws Exception {
        log.debug("REST request to delete Mesh : {}", id);
        meshService.deleteMesh(id, services.getIP());
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }
}
