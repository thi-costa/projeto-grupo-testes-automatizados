package br.com.service;

import br.com.domain.Carrinho;
import br.com.model.entity.ProdutoEntity;
import br.com.repository.CarrinhoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CarrinhoService {

    @Autowired
    private CarrinhoRepository repository;

    public void finalizarCompra(Carrinho request){
        request.getProdutos().forEach((idProduto, qtdCompra) ->{
            Optional<ProdutoEntity> produtoOp = repository.findById(idProduto);

            if (produtoOp.isEmpty()){
                throw new RuntimeException("Produto Inexistente!");
            }

            ProdutoEntity produto = produtoOp.get();

            if (qtdCompra < 0){
                throw new RuntimeException("Quantidade de compra negativa");
            } else if (qtdCompra > produto.getQuantidade()){
                throw new RuntimeException("Quantidade em estoque insuficiente");
            } else {
                if(produto.getEmPromocao().equals(Boolean.TRUE) && qtdCompra > 3){
                     throw new RuntimeException("Quantidade máxima de compra do item em promoção excedida (máximo 3 itens)");
                }
                produto.setQuantidade(produto.getQuantidade() - qtdCompra);

                repository.save(produto);
            }
        });
    }

}
