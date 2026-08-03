package com.campuseatery.config;

import com.campuseatery.model.MenuItem;
import com.campuseatery.model.Stall;
import com.campuseatery.model.User;
import com.campuseatery.repository.MenuItemRepository;
import com.campuseatery.repository.StallRepository;
import com.campuseatery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StallRepository stallRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            if (stallRepository.count() == 0) {
                // Seed Vendor User 1
                User vendorUser1 = new User();
                vendorUser1.setId("demo_vendor_user_1");
                vendorUser1.setEmail("spicybyte@campus.edu");
                vendorUser1.setRole("vendor");
                vendorUser1.setApprovalStatus("approved");
                userRepository.save(vendorUser1);

                Stall stall1 = new Stall();
                stall1.setId("stall_1");
                stall1.setVendorId(vendorUser1.getId());
                stall1.setName("The Spicy Byte Cafe");
                stall1.setDescription("Authentic Indian street food, thalis, and hot beverages.");
                stall1.setCollegeLocation("South Block Canteen");
                stall1.setRating(4.8);
                stall1.setPrepTimeMinutes(15);
                stall1.setIsActive(true);
                stall1.setIsDemo(true);
                stallRepository.save(stall1);

                MenuItem m1 = new MenuItem();
                m1.setStallId(stall1.getId());
                m1.setName("Paneer Butter Masala Combo");
                m1.setDescription("Rich tomato gravy paneer served with 2 Butter Naans.");
                m1.setPricePaise(18000); // ₹180.00
                m1.setCategory("North Indian");
                m1.setIsAvailable(true);

                MenuItem m2 = new MenuItem();
                m2.setStallId(stall1.getId());
                m2.setName("Cheese Grilled Sandwich");
                m2.setDescription("Triple-layered loaded cheese sandwich toasted to perfection.");
                m2.setPricePaise(9000); // ₹90.00
                m2.setCategory("Snacks");
                m2.setIsAvailable(true);

                MenuItem m3 = new MenuItem();
                m3.setStallId(stall1.getId());
                m3.setName("Cold Coffee with Ice Cream");
                m3.setDescription("Thick blended cold coffee topped with vanilla scoop.");
                m3.setPricePaise(8000); // ₹80.00
                m3.setCategory("Beverages");
                m3.setIsAvailable(true);

                menuItemRepository.saveAll(Arrays.asList(m1, m2, m3));

                // Seed Vendor User 2
                User vendorUser2 = new User();
                vendorUser2.setId("demo_vendor_user_2");
                vendorUser2.setEmail("campusbowl@campus.edu");
                vendorUser2.setRole("vendor");
                vendorUser2.setApprovalStatus("approved");
                userRepository.save(vendorUser2);

                Stall stall2 = new Stall();
                stall2.setId("stall_2");
                stall2.setVendorId(vendorUser2.getId());
                stall2.setName("Campus Bowl & Roll");
                stall2.setDescription("Kathi rolls, shawarmas, and refreshing rice bowls.");
                stall2.setCollegeLocation("Student Activity Center (SAC)");
                stall2.setRating(4.6);
                stall2.setPrepTimeMinutes(10);
                stall2.setIsActive(true);
                stall2.setIsDemo(true);
                stallRepository.save(stall2);

                MenuItem m4 = new MenuItem();
                m4.setStallId(stall2.getId());
                m4.setName("Chicken Kathi Roll");
                m4.setDescription("Juicy spicy chicken wrapped in flaky paratha.");
                m4.setPricePaise(12000); // ₹120.00
                m4.setCategory("Rolls");
                m4.setIsAvailable(true);

                MenuItem m5 = new MenuItem();
                m5.setStallId(stall2.getId());
                m5.setName("Paneer Tikka Bowl");
                m5.setDescription("Basmati rice topped with grilled paneer tikka & mint chutney.");
                m5.setPricePaise(15000); // ₹150.00
                m5.setCategory("Bowls");
                m5.setIsAvailable(true);

                MenuItem m6 = new MenuItem();
                m6.setStallId(stall2.getId());
                m6.setName("Mango Lassi");
                m6.setDescription("Chilled sweet yogurt smoothie with fresh mango pulp.");
                m6.setPricePaise(6000); // ₹60.00
                m6.setCategory("Beverages");
                m6.setIsAvailable(true);

                menuItemRepository.saveAll(Arrays.asList(m4, m5, m6));

                // Seed Vendor User 3
                User vendorUser3 = new User();
                vendorUser3.setId("demo_vendor_user_3");
                vendorUser3.setEmail("grillchill@campus.edu");
                vendorUser3.setRole("vendor");
                vendorUser3.setApprovalStatus("approved");
                userRepository.save(vendorUser3);

                Stall stall3 = new Stall();
                stall3.setId("stall_3");
                stall3.setVendorId(vendorUser3.getId());
                stall3.setName("Grill & Chill Diner");
                stall3.setDescription("Handcrafted burgers, peri peri fries, and thick milkshakes.");
                stall3.setCollegeLocation("Library Square");
                stall3.setRating(4.9);
                stall3.setPrepTimeMinutes(12);
                stall3.setIsActive(true);
                stall3.setIsDemo(true);
                stallRepository.save(stall3);

                MenuItem m7 = new MenuItem();
                m7.setStallId(stall3.getId());
                m7.setName("Smoky BBQ Veggie Burger");
                m7.setDescription("Crispy vegetable patty with melted cheese & BBQ sauce.");
                m7.setPricePaise(13000); // ₹130.00
                m7.setCategory("Burgers");
                m7.setIsAvailable(true);

                MenuItem m8 = new MenuItem();
                m8.setStallId(stall3.getId());
                m8.setName("Peri Peri Loaded Fries");
                m8.setDescription("Golden french fries dusted with spicy peri peri seasoning & cheese dip.");
                m8.setPricePaise(10000); // ₹100.00
                m8.setCategory("Sides");
                m8.setIsAvailable(true);

                menuItemRepository.saveAll(Arrays.asList(m7, m8));

                System.out.println(">>> Demo vendors and menu items initialized successfully!");
            }
        } catch (Exception e) {
            System.err.println(">>> DataInitializer failed to execute (database might be initializing or unreachable): " + e.getMessage());
        }
    }
}
