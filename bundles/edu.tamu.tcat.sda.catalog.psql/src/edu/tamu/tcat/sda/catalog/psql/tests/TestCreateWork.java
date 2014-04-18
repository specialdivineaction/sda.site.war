package edu.tamu.tcat.sda.catalog.psql.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import edu.tamu.tcat.sda.catalog.psql.PsqlWorkRepo;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDv;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;
import edu.tamu.tcat.sda.ds.DataUpdateObserver;

public class TestCreateWork 
{

	@Test
	public void testCreate() 
	{
		AuthorRefDv authorRef = new AuthorRefDv();
		authorRef.authorId = "1234";
		authorRef.name = "A.C. Dixon";
		authorRef.role = "";
		
		List<AuthorRefDv> authorList = new ArrayList<>();
		authorList.add(authorRef);
		
		
		WorkDV works = new WorkDV();
		works.id = UUID.randomUUID().toString();
		works.authors = authorList;
		
		PsqlWorkRepo workRepo = new PsqlWorkRepo();
		DataUpdateObserver<WorkDV, Work> observer = null;
      workRepo.create(works, observer);
		
		
		fail("Not yet implemented");
	}

}
