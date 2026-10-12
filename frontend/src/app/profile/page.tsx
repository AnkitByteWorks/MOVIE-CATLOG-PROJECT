'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import Link from 'next/link';
import { format } from 'date-fns';
import {
  Calendar,
  Clock,
  ExternalLink,
  Eye,
  EyeOff,
  Globe,
  KeyRound,
  Loader2,
  Lock,
  LogOut,
  Mail,
  MapPin,
  Save,
  User as UserIcon,
} from 'lucide-react';
import { ProtectedRoute } from '@/components/auth/ProtectedRoute';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Textarea } from '@/components/ui/textarea';
import { useAuth, useChangePassword, useLogout, useMe, useUpdateProfile } from '@/lib/hooks/useAuth';

// Validation schemas
const profileSchema = z.object({
  displayName: z.string().max(50, 'Display name cannot exceed 50 characters').optional(),
  bio: z.string().max(250, 'Bio cannot exceed 250 characters').optional(),
  avatarUrl: z.string().url('Must be a valid URL').or(z.literal('')).optional(),
  location: z.string().max(100, 'Location cannot exceed 100 characters').optional(),
  website: z.string().url('Must be a valid URL').or(z.literal('')).optional(),
});

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword: z
      .string()
      .min(8, 'New password must be at least 8 characters')
      .max(72, 'Password cannot exceed 72 characters')
      .regex(/[A-Z]/, 'Must contain at least one uppercase letter')
      .regex(/[a-z]/, 'Must contain at least one lowercase letter')
      .regex(/[0-9]/, 'Must contain at least one digit')
      .regex(/[^a-zA-Z0-9]/, 'Must contain at least one special character'),
    confirmNewPassword: z.string().min(1, 'Please confirm your new password'),
  })
  .refine((data) => data.newPassword === data.confirmNewPassword, {
    message: 'New passwords do not match',
    path: ['confirmNewPassword'],
  });

type ProfileFormData = z.infer<typeof profileSchema>;
type PasswordFormData = z.infer<typeof passwordSchema>;

function ProfileContent() {
  const { user } = useAuth();
  const { data: meData } = useMe();
  const updateProfileMutation = useUpdateProfile();
  const changePasswordMutation = useChangePassword();
  const logoutMutation = useLogout();

  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const currentUser = meData || user;

  // Profile form
  const {
    register: registerProfile,
    handleSubmit: handleProfileSubmit,
    reset: resetProfile,
    formState: { errors: profileErrors },
  } = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      displayName: currentUser?.displayName || '',
      bio: currentUser?.bio || '',
      avatarUrl: currentUser?.avatarUrl || '',
      location: currentUser?.location || '',
      website: currentUser?.website || '',
    },
  });

  // Sync form when fresh profile data arrives
  useEffect(() => {
    if (currentUser) {
      resetProfile({
        displayName: currentUser.displayName || '',
        bio: currentUser.bio || '',
        avatarUrl: currentUser.avatarUrl || '',
        location: currentUser.location || '',
        website: currentUser.website || '',
      });
    }
  }, [currentUser, resetProfile]);

  // Password form
  const {
    register: registerPassword,
    handleSubmit: handlePasswordSubmit,
    reset: resetPassword,
    formState: { errors: passwordErrors },
  } = useForm<PasswordFormData>({
    resolver: zodResolver(passwordSchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmNewPassword: '',
    },
  });

  const onProfileSubmit = (data: ProfileFormData) => {
    updateProfileMutation.mutate({
      displayName: data.displayName || undefined,
      bio: data.bio || undefined,
      avatarUrl: data.avatarUrl || undefined,
      location: data.location || undefined,
      website: data.website || undefined,
    });
  };

  const onPasswordSubmit = (data: PasswordFormData) => {
    changePasswordMutation.mutate(
      {
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      },
      {
        onSuccess: () => {
          resetPassword();
        },
      }
    );
  };

  const initials = (currentUser?.displayName || currentUser?.username || 'U')
    .slice(0, 2)
    .toUpperCase();

  const formattedJoinDate = currentUser?.createdAt
    ? format(new Date(currentUser.createdAt), 'MMMM yyyy')
    : 'Recently';

  const formattedLastLogin = currentUser?.lastLoginAt
    ? format(new Date(currentUser.lastLoginAt), 'MMM dd, yyyy · HH:mm')
    : 'First session';

  return (
    <div className="min-h-screen pt-28 pb-20 px-4 max-w-4xl mx-auto space-y-8">
      {/* Profile Header Card */}
      <Card className="bg-[#131318]/90 border-white/10 backdrop-blur-xl shadow-2xl text-white overflow-hidden relative">
        <div className="h-32 bg-gradient-to-r from-rose-950/60 via-purple-950/40 to-amber-950/40 border-b border-white/10 relative">
          <div className="absolute top-4 right-4">
            <Button
              variant="outline"
              size="sm"
              onClick={() => logoutMutation.mutate()}
              disabled={logoutMutation.isPending}
              className="border-rose-500/30 text-rose-300 hover:bg-rose-950/30 text-xs backdrop-blur-md"
            >
              {logoutMutation.isPending ? (
                <Loader2 className="w-3.5 h-3.5 animate-spin mr-1.5" />
              ) : (
                <LogOut className="w-3.5 h-3.5 mr-1.5" />
              )}
              Sign Out
            </Button>
          </div>
        </div>

        <CardContent className="pt-0 pb-6 px-6 sm:px-8 relative">
          <div className="flex flex-col sm:flex-row items-start sm:items-end justify-between gap-4 -mt-14 mb-4">
            <div className="flex items-end gap-4">
              <Avatar className="h-24 w-24 border-4 border-[#131318] shadow-2xl bg-zinc-800">
                {currentUser?.avatarUrl && <AvatarImage src={currentUser.avatarUrl} alt={currentUser.username} />}
                <AvatarFallback className="bg-gradient-to-br from-rose-600 to-amber-600 text-white font-bold text-xl">
                  {initials}
                </AvatarFallback>
              </Avatar>
              <div className="space-y-1 mb-1">
                <div className="flex items-center gap-2">
                  <h1 className="text-2xl font-bold font-serif">
                    {currentUser?.displayName || currentUser?.username}
                  </h1>
                  <Badge variant="outline" className="border-rose-500/40 text-rose-300 text-[10px] px-2 py-0.5">
                    {currentUser?.role || 'USER'}
                  </Badge>
                </div>
                <p className="text-xs text-zinc-400 font-mono">@{currentUser?.username}</p>
              </div>
            </div>

            <div className="flex items-center gap-2 w-full sm:w-auto">
              <Link href="/watchlog" className="w-full sm:w-auto">
                <Button variant="outline" size="sm" className="w-full text-xs border-white/10 hover:bg-white/5 text-zinc-300">
                  Watch History
                </Button>
              </Link>
              <Link href="/stats" className="w-full sm:w-auto">
                <Button size="sm" className="w-full text-xs bg-rose-600 hover:bg-rose-500 text-white">
                  My Stats
                </Button>
              </Link>
            </div>
          </div>

          {currentUser?.bio && (
            <p className="text-sm text-zinc-300 mt-2 max-w-2xl leading-relaxed">{currentUser.bio}</p>
          )}

          <div className="flex flex-wrap items-center gap-y-2 gap-x-6 text-xs text-zinc-400 pt-4 border-t border-white/5 mt-4">
            <div className="flex items-center gap-1.5">
              <Mail className="h-3.5 w-3.5 text-zinc-500" />
              <span>{currentUser?.email}</span>
            </div>
            {currentUser?.location && (
              <div className="flex items-center gap-1.5">
                <MapPin className="h-3.5 w-3.5 text-zinc-500" />
                <span>{currentUser.location}</span>
              </div>
            )}
            {currentUser?.website && (
              <div className="flex items-center gap-1.5">
                <Globe className="h-3.5 w-3.5 text-zinc-500" />
                <a
                  href={currentUser.website}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-rose-400 hover:underline flex items-center gap-1"
                >
                  {currentUser.website.replace(/^https?:\/\//, '')}
                  <ExternalLink className="h-3 w-3" />
                </a>
              </div>
            )}
            <div className="flex items-center gap-1.5">
              <Calendar className="h-3.5 w-3.5 text-zinc-500" />
              <span>Joined {formattedJoinDate}</span>
            </div>
            <div className="flex items-center gap-1.5">
              <Clock className="h-3.5 w-3.5 text-zinc-500" />
              <span>Last active {formattedLastLogin}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs: Edit Profile & Security */}
      <Tabs defaultValue="edit-profile" className="space-y-6">
        <TabsList className="bg-[#131318] border border-white/10 p-1 rounded-xl">
          <TabsTrigger
            value="edit-profile"
            className="data-[state=active]:bg-rose-600 data-[state=active]:text-white text-zinc-400 text-xs px-4 py-2 rounded-lg"
          >
            <UserIcon className="w-3.5 h-3.5 mr-1.5" />
            Edit Profile
          </TabsTrigger>
          <TabsTrigger
            value="security"
            className="data-[state=active]:bg-rose-600 data-[state=active]:text-white text-zinc-400 text-xs px-4 py-2 rounded-lg"
          >
            <Lock className="w-3.5 h-3.5 mr-1.5" />
            Password & Security
          </TabsTrigger>
        </TabsList>

        {/* Tab 1: Edit Profile */}
        <TabsContent value="edit-profile">
          <Card className="bg-[#131318]/90 border-white/10 text-white backdrop-blur-xl">
            <CardHeader>
              <CardTitle className="text-lg font-bold font-serif">Profile Information</CardTitle>
              <CardDescription className="text-xs text-zinc-400">
                Update your public profile details, bio, and social links
              </CardDescription>
            </CardHeader>

            <form onSubmit={handleProfileSubmit(onProfileSubmit)}>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-zinc-300 uppercase tracking-wider">
                      Display Name
                    </label>
                    <Input
                      placeholder="Your name"
                      disabled={updateProfileMutation.isPending}
                      {...registerProfile('displayName')}
                      className="bg-black/40 border-white/10 text-white placeholder:text-zinc-600 focus-visible:ring-rose-500"
                    />
                    {profileErrors.displayName && (
                      <p className="text-xs text-rose-400">{profileErrors.displayName.message}</p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-zinc-300 uppercase tracking-wider">
                      Avatar Image URL
                    </label>
                    <Input
                      placeholder="https://..."
                      disabled={updateProfileMutation.isPending}
                      {...registerProfile('avatarUrl')}
                      className="bg-black/40 border-white/10 text-white placeholder:text-zinc-600 focus-visible:ring-rose-500"
                    />
                    {profileErrors.avatarUrl && (
                      <p className="text-xs text-rose-400">{profileErrors.avatarUrl.message}</p>
                    )}
                  </div>
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-zinc-300 uppercase tracking-wider">Bio</label>
                  <Textarea
                    placeholder="Film enthusiast, cinema reviewer, sci-fi lover..."
                    rows={3}
                    disabled={updateProfileMutation.isPending}
                    {...registerProfile('bio')}
                    className="bg-black/40 border-white/10 text-white placeholder:text-zinc-600 focus-visible:ring-rose-500 resize-none"
                  />
                  {profileErrors.bio && <p className="text-xs text-rose-400">{profileErrors.bio.message}</p>}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-zinc-300 uppercase tracking-wider">
                      Location
                    </label>
                    <Input
                      placeholder="Tokyo, Japan"
                      disabled={updateProfileMutation.isPending}
                      {...registerProfile('location')}
                      className="bg-black/40 border-white/10 text-white placeholder:text-zinc-600 focus-visible:ring-rose-500"
                    />
                    {profileErrors.location && (
                      <p className="text-xs text-rose-400">{profileErrors.location.message}</p>
                    )}
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-zinc-300 uppercase tracking-wider">
                      Website / Portfolio
                    </label>
                    <Input
                      placeholder="https://letterboxd.com/..."
                      disabled={updateProfileMutation.isPending}
                      {...registerProfile('website')}
                      className="bg-black/40 border-white/10 text-white placeholder:text-zinc-600 focus-visible:ring-rose-500"
                    />
                    {profileErrors.website && (
                      <p className="text-xs text-rose-400">{profileErrors.website.message}</p>
                    )}
                  </div>
                </div>
              </CardContent>

              <CardFooter className="border-t border-white/5 pt-4 flex justify-end">
                <Button
                  type="submit"
                  disabled={updateProfileMutation.isPending}
                  className="bg-rose-600 hover:bg-rose-500 text-white font-medium text-xs shadow-lg shadow-rose-600/20"
                >
                  {updateProfileMutation.isPending ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Saving Changes...
                    </>
                  ) : (
                    <>
                      <Save className="mr-2 h-4 w-4" />
                      Save Changes
                    </>
                  )}
                </Button>
              </CardFooter>
            </form>
          </Card>
        </TabsContent>

        {/* Tab 2: Security */}
        <TabsContent value="security">
          <Card className="bg-[#131318]/90 border-white/10 text-white backdrop-blur-xl">
            <CardHeader>
              <CardTitle className="text-lg font-bold font-serif">Change Password</CardTitle>
              <CardDescription className="text-xs text-zinc-400">
                Ensure your account is protected with a strong, distinct password
              </CardDescription>
            </CardHeader>

            <form onSubmit={handlePasswordSubmit(onPasswordSubmit)}>
              <CardContent className="space-y-4 max-w-lg">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-zinc-300 uppercase tracking-wider">
                    Current Password *
                  </label>
                  <div className="relative">
                    <Input
                      type={showCurrentPassword ? 'text' : 'password'}
                      placeholder="••••••••"
                      disabled={changePasswordMutation.isPending}
                      {...registerPassword('currentPassword')}
                      className="bg-black/40 border-white/10 text-white placeholder:text-zinc-600 pr-10 focus-visible:ring-rose-500"
                    />
                    <button
                      type="button"
                      tabIndex={-1}
                      onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-white"
                    >
                      {showCurrentPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                  {passwordErrors.currentPassword && (
                    <p className="text-xs text-rose-400">{passwordErrors.currentPassword.message}</p>
                  )}
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-zinc-300 uppercase tracking-wider">
                    New Password *
                  </label>
                  <div className="relative">
                    <Input
                      type={showNewPassword ? 'text' : 'password'}
                      placeholder="••••••••"
                      disabled={changePasswordMutation.isPending}
                      {...registerPassword('newPassword')}
                      className="bg-black/40 border-white/10 text-white placeholder:text-zinc-600 pr-10 focus-visible:ring-rose-500"
                    />
                    <button
                      type="button"
                      tabIndex={-1}
                      onClick={() => setShowNewPassword(!showNewPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-white"
                    >
                      {showNewPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                  {passwordErrors.newPassword && (
                    <p className="text-xs text-rose-400">{passwordErrors.newPassword.message}</p>
                  )}
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-zinc-300 uppercase tracking-wider">
                    Confirm New Password *
                  </label>
                  <div className="relative">
                    <Input
                      type={showConfirmPassword ? 'text' : 'password'}
                      placeholder="••••••••"
                      disabled={changePasswordMutation.isPending}
                      {...registerPassword('confirmNewPassword')}
                      className="bg-black/40 border-white/10 text-white placeholder:text-zinc-600 pr-10 focus-visible:ring-rose-500"
                    />
                    <button
                      type="button"
                      tabIndex={-1}
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-white"
                    >
                      {showConfirmPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                  {passwordErrors.confirmNewPassword && (
                    <p className="text-xs text-rose-400">{passwordErrors.confirmNewPassword.message}</p>
                  )}
                </div>
              </CardContent>

              <CardFooter className="border-t border-white/5 pt-4 flex justify-end">
                <Button
                  type="submit"
                  disabled={changePasswordMutation.isPending}
                  className="bg-rose-600 hover:bg-rose-500 text-white font-medium text-xs shadow-lg shadow-rose-600/20"
                >
                  {changePasswordMutation.isPending ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Updating Password...
                    </>
                  ) : (
                    <>
                      <KeyRound className="mr-2 h-4 w-4" />
                      Update Password
                    </>
                  )}
                </Button>
              </CardFooter>
            </form>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}

export default function ProfilePage() {
  return (
    <ProtectedRoute>
      <ProfileContent />
    </ProtectedRoute>
  );
}
