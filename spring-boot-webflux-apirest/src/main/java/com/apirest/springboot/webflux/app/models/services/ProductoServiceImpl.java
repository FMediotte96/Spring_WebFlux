package com.apirest.springboot.webflux.app.models.services;

import com.apirest.springboot.webflux.app.models.dao.CategoriaDAO;
import com.apirest.springboot.webflux.app.models.dao.ProductoDAO;
import com.apirest.springboot.webflux.app.models.documents.Categoria;
import com.apirest.springboot.webflux.app.models.documents.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoDAO dao;

    private final CategoriaDAO categoriaDAO;

    @Autowired
    public ProductoServiceImpl(ProductoDAO dao, CategoriaDAO categoriaDAO) {
        this.dao = dao;
        this.categoriaDAO = categoriaDAO;
    }

    @Override
    public Flux<Producto> findAll() {
        return dao.findAll();
    }

    @Override
    public Flux<Producto> findAllWithNameUpperCase() {
        return dao.findAll().map(producto -> {
            producto.setNombre(producto.getNombre().toUpperCase());
            return producto;
        });
    }

    @Override
    public Flux<Producto> findAllWithNameUpperCaseAndRepeat() {
        return findAllWithNameUpperCase().repeat(5000);
    }

    @Override
    public Mono<Producto> findById(String id) {
        return dao.findById(id);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return dao.save(producto);
    }

    @Override
    public Mono<Void> delete(Producto producto) {
        return dao.delete(producto);
    }

    @Override
    public Flux<Categoria> findAllCategoria() {
        return categoriaDAO.findAll();
    }

    @Override
    public Mono<Categoria> findCategoriaById(String id) {
        return categoriaDAO.findById(id);
    }

    @Override
    public Mono<Categoria> saveCategoria(Categoria categoria) {
        return categoriaDAO.save(categoria);
    }
}
