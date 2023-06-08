package Group2.capstone_project.repository;

import Group2.capstone_project.domain.Client;
import Group2.capstone_project.domain.Club;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class MemoryClientRepository implements ClientRepository{

    private static List<Client> clients = new ArrayList<>();
    @Override
    public void save(Client client) {
        clients.add(client);
    }

    @Override
    public Optional<Client> findId(String name, String studentNumber,String age) {
       return clients.stream().filter(client -> client.getName().equals(name)
                &&client.getStudentNumber().equals(studentNumber)
                &&client.getAge().equals(age)).findAny();

    }

    @Override
    public Optional<Client> findById(String id) {
        return clients.stream().filter(client -> client.getId().equals(id)).findAny();

    }

    @Override
    public Optional<Client> login(Client client) {
        return Optional.empty();
    }

    @Override
    public void updatePwd(String id, String newEncodePwd) {

    }

    @Override
    public void authJoin(String id) {

    }

    @Override
    public void clientDelete(String id, String studentNumber) {

    }

    @Override
    public void updateInfo(Client client) {

    }

    @Override
    public List<Club> getClubByClient(String id) {
        return null;
    }


    @Override
    public Optional<Client> findPwd(String name, String id, String studentNumber, String question, String answer) {
        return clients.stream().filter(client -> client.getName().equals(name)
                 &&client.getId().equals(id)
                &&client.getStudentNumber().equals(studentNumber)).findAny();
    }

    @Override
    public List<Client> findAll()
    {
        return clients;
    }
}
