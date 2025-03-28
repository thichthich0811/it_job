package com.nvt.it_job.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.nvt.it_job.domain.Company;
import com.nvt.it_job.domain.User;
import com.nvt.it_job.domain.response.ResultPaginationDTO;
import com.nvt.it_job.repository.CompanyRepository;
import com.nvt.it_job.repository.UserRepository;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public Company handleCreateCompany(Company company) {
        return this.companyRepository.save(company);
    }

    public void handleDeleteCompany(long id) {
        Optional<Company> company = this.companyRepository.findById(id);
        if (company.isPresent()) {
            Company com = company.get();
            List<User> users = this.userRepository.findByCompany(com);
            this.userRepository.deleteAll(users);
        }
        companyRepository.deleteById(id);
    }

    public Company handleFetchCompanyById(long id) {
        Optional<Company> company = companyRepository.findById(id);
        if (company.isPresent()) {
            return company.get();
        }
        return null;
    }

    public ResultPaginationDTO handleFetchAllCompany(Specification<Company> spec, Pageable pageable) {
        Page<Company> pageCurrent = companyRepository.findAll(spec, pageable);
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageCurrent.getNumber() + 1);
        mt.setPageSize(pageCurrent.getSize());
        mt.setTotal(pageCurrent.getTotalElements());
        mt.setPages(pageCurrent.getTotalPages());

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        resultPaginationDTO.setMeta(mt);
        resultPaginationDTO.setResult(pageCurrent.getContent());
        return resultPaginationDTO;
    }

    public Company handleUpdateCompany(Company company) {
        Company company1 = handleFetchCompanyById(company.getId());
        if (company1 != null) {
            company1.setName(company.getName());
            company1.setLogo(company.getLogo());
            company1.setAddress(company.getAddress());
            company1.setDescription(company.getDescription());
        }
        return companyRepository.save(company1);
    }
}
