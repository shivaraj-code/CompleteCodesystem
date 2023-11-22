(1)from emp table
SELECT * FROM database_practice.emp;
select ename,sal,job from emp where job not between 'MANAGER' and 'SALESMAN';
select ename,sal,job from emp where job  between 'MANAGER' and 'SALESMAN';
SELECT ename,sal,job,hiredate from emp where hiredate not between '17-DEC-81' and '20-JUN-83';
SELECT ename,sal,job,hiredate from emp where hiredate  between '17-DEC-81' and '20-JUN-83';
SELECT * FROM EMP WHERE ENAME LIKE '%S';
SELECT * FROM EMP WHERE ENAME LIKE 'S%';
SELECT * FROM EMP WHERE ENAME LIKE '_L%';
SELECT * FROM EMP WHERE ENAME LIKE '%R_';
(2)from cpt table
SELECT * FROM kavanant_practice.cpt;
SELECT * FROM kavanant_practice.cpt where code in("00162");
select count(*)FROM kavanant_practiceuser.cpt;
alter table cpt add constraint unique_code unique(code);
update cpt set ref_id=id, original_ref_id=id where id>0;
alter table cpt drop constraint unique_code;
select * from kavanant_practice.cpt where status=Y or retired=N;
select distinct code,ref_id,version_state from cpt group By code having count(*)>1;
delete from cpt where code in('0000001','00100','00102','00103','00120','00140','00522') and id>0;
delete from cpt where code in("00148");
update cpt set Versicpt_shorton_State="Valid" where id>=1 and  version_state is null;
update cpt set version_state=NULL where id>=1 and  version_state="INVALID";
update cpt set retired='N' where id>0;
alter table cpt add constraint unique_code unique(code, retired, version_state);
alter table cpt drop column retired_on;
(3)from cpt_short Table
SELECT * FROM kavanant_practice.cpt_short where code="001144";
update cpt_short set ref_id=id, original_ref_id=id where id>0;
update cpt_short set version_state="Valid" where id>=1;
update cpt_short set retired="N" where id>=1;
update kavanant_practice.cpt_short set created_date=now() where id>0;
update kavanant_practice.cpt_short set modified_date=now() where id>0;
(4)from Medicines_new Table
SELECT * FROM codesystem.medicines;

SELECT count(*)FROM kavanant_practice.medicines_new;
update medicines_new set RefId=AIId, OriginalRefId=AIId where AIId>0;
SELECT * FROM kavanant_practice.medicines_new where retired='Y';
SELECT * FROM kavanant_practice.medicines_new where ndc="00078067515";
SELECT * FROM kavanant_practice.medicines_new where name ="Emgality 300 mg/3 mL (100 mg/mL x 3) subcutaneous syringe";
SELECT * FROM kavanant_practice.medicines_new where id ="170124";
SELECT * FROM kavanant_practice.medicines_new where ndc like "2%4";
SELECT * FROM kavanant_practice.medicines_new where ndc like "%000123";
select count(*) from kavanant_practice.medicines_new where name like "%gabapentin%";
select count(*) from kavanant_practice.medicines_new where name like "%gabapentin 300 mg%";
select * from kavanant_practice.medicines_new where name like "%Albuminar 25 % intravenous solution%";
select count(*) from kavanant_practice.medicines_new where name like "%gabapentin 400 mg%";
select * from kavanant_practice.medicines_new where name like "%zofran 8 mg tablet%";
select * from kavanant_practice.medicines_new where name like "%tinidazole 500 mg tablet%";
select @@global.time_zone;
SELECT @@global.time_zone, @@session.time_zone;
SELECT name, COUNT(*) AS "number of records"
FROM kavanant_practice.medicines_new
GROUP BY name;

SELECT * FROM kavanant_practice.medicines_new 
WHERE name LIKE '%Tylactin%' OR ndc LIKE '%24359059%'
AND version_state = 'Valid';

update icd10cm set VersionState="Valid" where id>=1 and  VersionState is null;
update kavanant_practice.medicines_new set CreatedDate=now() where AIId>0;
update kavanant_practice.medicines_new set lastModifiedDate=now() where AIId>0;
select count(*) FROM kavanant_practice.medicines_new;
select * from medicines_new where match(name,ndc) against("600 mg gabapentin") AND version_state='Valid';
alter table medicines_new add fulltext(name,ndc);
(5)from medicines table
SELECT count(*) FROM codesystem.medicines where version_state="Valid";
SELECT * FROM codesystem.medicines where version_state="Invalid";
SELECT * FROM codesystem.medicines where version_state="Valid";
SELECT * FROM codesystem.medicines where ndc="00078067515";


UPDATE codesystem.medicines
SET version_state = CASE
    WHEN version_state = 'Valid' THEN 'Validated'
    WHEN version_state = 'Invalid' THEN 'InValidated'
    ELSE version_state
END;
update codesystem.medicines set version_state='Validated' where version_state="Valid";
SELECT * FROM codesystem.medicines;
SELECT count(*) FROM codesystem.medicines where version_state="Valid";
SELECT count(*) FROM codesystem.medicines where version_state="Invalid";
SELECT * FROM codesystem.medicines where version_state="Valid";
SELECT * FROM codesystem.medicines where ndc="00078067515";


UPDATE codesystem.medicines
SET version_state = CASE
    WHEN version_state = 'Valid' THEN 'Validated'
    WHEN version_state = 'Invalid' THEN 'InValidated'
    ELSE version_state
END;
update codesystem.medicines set version_state='Validated' where version_state="Valid";
update codesystem.medicines set version_state='InValidated' where version_state="Invalid";

SET SQL_SAFE_UPDATES = 1;
SELECT * FROM codesystem.medicines;
update codesystem.medicines set ref_id=aiid, original_ref_id=aiid where aiid>0;
update codesystem.medicines set status='Y' where aiid>0;
update codesystem.medicines set created_date=now() where aiid>0;
update codesystem.medicines set last_modified_date=now() where aiid>0;
update codesystem.medicines set retired='N' where retired='Y';
SET SQL_SAFE_UPDATES = 1;



