package br.com.controller;

import br.com.domain.Produto;
import br.com.model.entity.ProdutoEntity;
import br.com.repository.CarrinhoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CarrinhoControllerTest {
    @BeforeAll
    public void init(){
        ProdutoEntity entidade = new ProdutoEntity();
        entidade.setNome("Copo");
        entidade.setDescricao("250ml");
        entidade.setValor(5d);
        entidade.setQuantidade(10);

        repository.save(entidade);

        ProdutoEntity entidade2 = new ProdutoEntity();
        entidade2.setNome("Vinho");
        entidade2.setDescricao("250ml");
        entidade2.setValor(50d);
        entidade2.setQuantidade(15);
        entidade2.setEmPromocao(Boolean.TRUE);

        repository.save(entidade2);

        repository.delete(entidade2);

        ProdutoEntity entidade3 = new ProdutoEntity();
        entidade3.setNome("Cachaça");
        entidade3.setDescricao("1 L");
        entidade3.setValor(4.99d);
        entidade3.setQuantidade(10);
        entidade3.setEmPromocao(Boolean.TRUE);

        repository.save(entidade3);

    }
    @Autowired
    private CarrinhoController controller;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarrinhoRepository repository;


    @Test
    public void carregouContexto(){
        Assertions.assertTrue(controller != null);
    }

    @Test
    public void testeOk() throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/carrinho"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void efetuarCompraComSucesso() throws Exception {
        ProdutoEntity entidade = repository.findById(1).get();

        entidade.setQuantidade(10);
        entidade.setEmPromocao(Boolean.FALSE);
        repository.save(entidade);

        CarrinhoRequest carrinho = new CarrinhoRequest();
        carrinho.getProdutos().put(1, 2);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(carrinho);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/carrinho")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        ProdutoEntity produto = repository.findById(1).get();
        Assertions.assertEquals(8, produto.getQuantidade());
    }
    @Test
    public void efetuarCompraComFalhaQtdeInsuficienteEstoque() throws Exception {
        ProdutoEntity entidade = repository.findById(1).get();

        entidade.setQuantidade(1);
        entidade.setEmPromocao(Boolean.FALSE);
        repository.save(entidade);

        CarrinhoRequest carrinho = new CarrinhoRequest();
        carrinho.getProdutos().put(1, 2);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(carrinho);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/carrinho")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        ProdutoEntity produto = repository.findById(1).get();
        Assertions.assertEquals(1, produto.getQuantidade());
    }
    @Test
    public void efetuarCompraComFalhaQtdeNegativa() throws Exception {
        ProdutoEntity entidade = repository.findById(1).get();

        entidade.setQuantidade(1);
        entidade.setEmPromocao(Boolean.FALSE);
        repository.save(entidade);

        CarrinhoRequest carrinho = new CarrinhoRequest();
        carrinho.getProdutos().put(1, -1);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(carrinho);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/carrinho")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        ProdutoEntity produto = repository.findById(1).get();
        Assertions.assertEquals(1, produto.getQuantidade());
    }
    @Test
    public void efetuarCompraComFalhaProdutoInexistente() throws Exception {

        ProdutoEntity entidade = repository.findById(1).get();

        entidade.setQuantidade(1);
        entidade.setEmPromocao(Boolean.FALSE);
        repository.save(entidade);

        CarrinhoRequest carrinho = new CarrinhoRequest();
        carrinho.getProdutos().put(2, 1);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(carrinho);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/carrinho")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

        ProdutoEntity produto = repository.findById(1).get();
        Assertions.assertEquals(1, produto.getQuantidade());
    }
    @Test
    public void efetuarCompraComPromocaoComSucesso() throws Exception {
        ProdutoEntity entidade = repository.findById(1).get();
        entidade.setQuantidade(10);
        entidade.setEmPromocao(Boolean.TRUE);
        repository.save(entidade);

        CarrinhoRequest carrinho = new CarrinhoRequest();
        carrinho.getProdutos().put(1, 3);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(carrinho);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/carrinho")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        ProdutoEntity produto = repository.findById(1).get();
        Assertions.assertEquals(7, produto.getQuantidade());
    }
    @Test
    public void efetuarCompraComPromocaoComErro() throws Exception {

        ProdutoEntity entidade = repository.findById(1).get();

        entidade.setQuantidade(10);
        entidade.setEmPromocao(Boolean.TRUE);
        repository.save(entidade);

        CarrinhoRequest carrinho = new CarrinhoRequest();
        carrinho.getProdutos().put(1, 4);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(carrinho);


        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/carrinho")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());



        ProdutoEntity produto = repository.findById(1).get();

        Assertions.assertEquals(10, produto.getQuantidade());
    }
    @Test
    public void checarAutoIncremento() {
        ProdutoEntity entidade1 = repository.findById(1).get();

        Optional<ProdutoEntity> entidade2 = repository.findById(2);

        ProdutoEntity entidade3 = repository.findById(3).get();

        Assertions.assertEquals(1, entidade1.getId());
        Assertions.assertEquals(Optional.empty(), entidade2);
        Assertions.assertEquals(3, entidade3.getId());
    }

}
